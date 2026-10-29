package com.pcdd.sonovel.web.servlet;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.pcdd.sonovel.ai.HtmlTextUtil;
import com.pcdd.sonovel.ai.ProofreadService;
import com.pcdd.sonovel.core.AppConfigLoader;
import com.pcdd.sonovel.model.AppConfig;
import com.pcdd.sonovel.web.model.DownloadProgressInfo;
import com.pcdd.sonovel.web.util.RespUtils;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 对「服务器上已下载的成品书」执行 AI 校对（错别字 + 乱码）。
 * <p>
 * 校一整本书是分钟级乃至小时级的长任务，绝不能占着 HTTP 请求干等：
 * <ol>
 *   <li>先校验参数，立刻返回「已受理」，真正的校对放到虚拟线程；</li>
 *   <li>进度通过既有 SSE 通道（/download-progress）以 proofread-* 类型推送；</li>
 *   <li>同一时刻只允许一个校对任务，避免并发写同一本书互相覆盖。</li>
 * </ol>
 * 安全：只允许操作下载目录内的文件（与删除/下载接口一致的路径穿越防护），
 * 且写回前自动备份 {@code .proofread.bak}。
 */
public class BookProofreadServlet extends HttpServlet {

    private static final AtomicBoolean RUNNING = new AtomicBoolean(false);

    /**
     * GET /book-proofread?filename=xxx.txt  —— 受理后立即返回，进度走 SSE
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        String filename = req.getParameter("filename");
        if (StrUtil.isBlank(filename)) {
            RespUtils.writeError(resp, 400, "filename 参数不能为空");
            return;
        }

        AppConfig cfg = AppConfigLoader.APP_CONFIG;
        if (!cfg.getAi().garbledFixEnabled() && !cfg.getAi().usable()) {
            RespUtils.writeError(resp, 400, "AI 校对未启用：请在设置中打开 [ai] enabled 并填写服务地址与密钥");
            return;
        }

        File file = resolveInDownloadDir(resp, filename);
        if (file == null) return;

        if (!file.exists() || !file.isFile()) {
            RespUtils.writeError(resp, 404, "文件不存在");
            return;
        }
        String ext = FileUtil.extName(file.getName()).toLowerCase();
        if (!ext.equals("txt") && !ext.equals("html")) {
            RespUtils.writeError(resp, 400, "只能校对 TXT/HTML 成品；epub/pdf 请改用 txt/html 格式重新下载后再校对");
            return;
        }

        if (!RUNNING.compareAndSet(false, true)) {
            RespUtils.writeError(resp, 409, "已有校对任务在进行中，请等待其完成");
            return;
        }

        RespUtils.writeJson(resp, JSONUtil.createObj()
                .set("accepted", true)
                .set("filename", file.getName())
                .set("message", "校对任务已启动，进度见页面提示"));

        Thread.ofVirtual().name("proofread-" + file.getName()).start(() -> {
            try {
                runProofread(cfg, file);
            } catch (Exception e) {
                push("proofread-done", 0, 0, "校对失败：" + e.getMessage());
            } finally {
                RUNNING.set(false);
            }
        });
    }

    private void runProofread(AppConfig cfg, File file) {
        String[] meta = parseName(FileUtil.mainName(file.getName()));

        push("proofread-start", 0, 0,
                StrUtil.format("开始校对《{}》（{}）", meta[0], file.getName()));

        String summary = new ProofreadService(cfg).proofreadFiles(
                List.of(file), meta[0], meta[1], headIntro(file), file.getParentFile(), true,
                new ProofreadService.Progress() {
                    /**
                     * 整本书存在一个文件里，章级进度永远停在 0/1；
                     * 真正能反映「进行到哪」的是文件内部的送审块进度。
                     */
                    @Override
                    public void onChunkProgress(String fileName, int completed, int total) {
                        push("proofread-progress", completed, total, null);
                    }
                });

        push("proofread-done", 1, 1, summary);
    }

    /**
     * 通过既有 SSE 通道推送校对事件（无在线客户端时静默丢弃）
     */
    private static void push(String type, int index, int total, String message) {
        DownloadProgressSseServlet.sendProgress(JSONUtil.toJsonStr(DownloadProgressInfo.builder()
                .type(type)
                .index((long) index)
                .total(total)
                .message(message)
                .build()));
    }

    /**
     * 限定在下载目录内解析文件名，拒绝路径穿越
     */
    private File resolveInDownloadDir(HttpServletResponse resp, String filename) {
        try {
            String baseDir = new File(AppConfigLoader.APP_CONFIG.getDownloadPath()).getCanonicalPath();
            File file = new File(AppConfigLoader.APP_CONFIG.getDownloadPath(), filename).getCanonicalFile();
            if (!file.getPath().startsWith(baseDir + File.separator)) {
                RespUtils.writeError(resp, 403, "非法路径");
                return null;
            }
            return file;
        } catch (Exception e) {
            RespUtils.writeError(resp, 400, "路径解析失败：" + e.getMessage());
            return null;
        }
    }

    /**
     * 从文件名解析「书名 (作者)」，与下载器的成品命名一致
     */
    private String[] parseName(String name) {
        int l = name.lastIndexOf('(');
        int r = name.lastIndexOf(')');
        if (l > 0 && r > l) {
            return new String[]{name.substring(0, l).trim(), name.substring(l + 1, r).trim()};
        }
        return new String[]{name.trim(), ""};
    }

    /**
     * TXT 首页固定写入「书名/作者/简介」，据此取简介作为无百科条目时的降级背景
     */
    private String headIntro(File f) {
        try {
            String head = StrUtil.maxLength(FileUtil.readString(f, StandardCharsets.UTF_8), 4000);
            int idx = head.indexOf("简介：");
            if (idx < 0) return "";
            String rest = head.substring(idx + 3);
            int end = rest.indexOf("\n\n");
            if (end < 0) end = Math.min(rest.length(), 500);
            return HtmlTextUtil.text(rest.substring(0, end)).trim();
        } catch (Exception e) {
            return "";
        }
    }

}
