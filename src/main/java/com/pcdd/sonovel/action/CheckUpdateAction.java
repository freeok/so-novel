package com.pcdd.sonovel.action;

import cn.hutool.core.comparator.VersionComparator;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.StreamProgress;
import cn.hutool.core.lang.Console;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.Header;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.hutool.setting.dialect.Props;
import cn.hutool.system.OsInfo;
import cn.hutool.system.SystemUtil;
import com.pcdd.sonovel.util.ConfigUtils;
import com.pcdd.sonovel.util.RandomUA;
import me.tongfei.progressbar.ProgressBar;

import java.io.File;

import static org.fusesource.jansi.AnsiRenderer.render;

/**
 * @author pcdd
 * Created at 2024/11/10
 */
public class CheckUpdateAction {

    public static final String GHP = "https://ghp.ci/";
    public static final String RELEASE_URL = "https://api.github.com/repos/freeok/so-novel/releases";
    public static final String ASSETS_URL = "https://github.com/freeok/so-novel/releases/download/{}/sonovel-{}.tar.gz";
    private final int timeoutMills;

    public CheckUpdateAction() {
        this.timeoutMills = 10_000;
    }

    public CheckUpdateAction(int timeout) {
        this.timeoutMills = timeout;
    }

    public void execute() {
        Console.log("<== 检查更新中...");

        try (HttpResponse resp = HttpUtil.createGet(RELEASE_URL)
                .timeout(timeoutMills)
                .header(Header.USER_AGENT, RandomUA.generate())
                .execute()) {

            Props sys = ConfigUtils.sys();
            String jsonStr = resp.body();
            JSONArray arr = JSONUtil.parseArray(jsonStr);
            JSONObject latest = JSONUtil.parseObj(arr.get(0));
            // v1.7.0-beta.2
            String currentVersion = ("v" + sys.getStr("version"));
            // v1.7.0
            String latestVersion = latest.get("tag_name", String.class);
            String latestUrl = latest.get("html_url", String.class);

            if (VersionComparator.INSTANCE.compare(currentVersion, latestVersion) < 0) {
                Console.log("<== 发现新版本: {} ({})", latestVersion, latestUrl);
                download(getDownloadUrl(latestVersion));
            } else {
                Console.log("<== {} 已是最新版本！({})", latestVersion, latestUrl);
            }
        } catch (Exception e) {
            Console.log(render("@|red <== 更新失败，当前网络环境暂时无法访问 GitHub，请稍后再试 ({})|@"), e.getMessage());
        }
    }

    private String getDownloadUrl(String version) {
        OsInfo osInfo = SystemUtil.getOsInfo();
        String osName = osInfo.getName();
        String arch = osInfo.getArch();
        String fileName = "windows";

        if (osName.contains("Windows")) {
            fileName = "windows";
        } else if (osName.contains("Mac")) {
            // 根据架构进一步细分
            fileName = "aarch64".equals(arch) ? "macos_arm64" : "macos_x64";
        } else if (osName.contains("Linux")) {
            fileName = "linux";
        }

        return GHP + StrUtil.format(ASSETS_URL, version, fileName);
    }

    private void download(String url) {
        // HEAD 请求不会下载文件内容，只会返回文件的元数据（例如文件大小）
        HttpResponse resp = HttpUtil.createRequest(Method.HEAD, url)
                .timeout(10_000)
                .execute();
        long fileSize = resp.contentLength();
        resp.close();

        // 设置进度条
        ProgressBar pb = new ProgressBar("下载最新版", fileSize);
        //  下载到上一级路径
        File file = new File(System.getProperty("user.dir")).getParentFile();

        // 带进度显示的文件下载
        HttpUtil.downloadFile(url, file, new StreamProgress() {
            @Override
            public void start() {
                pb.setExtraMessage("下载中...");
            }

            @Override
            public void progress(long total, long step) {
                // 更新进度条
                pb.stepTo(step);
            }

            @Override
            public void finish() {
                pb.setExtraMessage("下载完成");
                Console.log("<== 下载位置: {}", file + File.separator + FileUtil.getName(url));
            }
        });

        pb.close();
    }

}