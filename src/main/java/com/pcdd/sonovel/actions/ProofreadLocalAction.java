package com.pcdd.sonovel.actions;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Console;
import cn.hutool.core.util.StrUtil;
import com.pcdd.sonovel.ai.HtmlTextUtil;
import com.pcdd.sonovel.ai.ProofreadService;
import com.pcdd.sonovel.model.AppConfig;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static org.fusesource.jansi.AnsiRenderer.render;

/**
 * 对「已下载好的本地书籍」执行校对（错别字 + 乱码）。
 * <p>
 * 与下载流程内置校对的差别只在元信息来源：成品文件的书名/作者从文件名推断
 * （约定命名 {@code 书名 (作者).txt}，即下载器的输出格式），简介取文件头部
 * （TXT 首页固定写入「书名/作者/简介」三行）。
 * <p>
 * 流程与内置校对一致：先做百科背景调研，再逐章改字，写回前自动备份 .proofread.bak。
 *
 * @author pcdd
 */
@RequiredArgsConstructor
public class ProofreadLocalAction {

    private final AppConfig config;
    private final Scanner sc = Console.scanner();

    /**
     * 交互式入口（TUI）
     */
    public void execute() {
        if (!config.getAi().garbledFixEnabled() && !config.getAi().usable()) {
            Console.error(render("AI 校对未启用。请在 config.ini 的 [ai] 段设置 enabled = 1，"
                    + "并填写 base-url / api-key / model（密钥也可用环境变量 SONOVEL_AI_KEY 提供）", "red"));
            return;
        }

        Console.print(render("==> 请输入要校对的 TXT/HTML 文件或章节目录: ", "green"));
        String input = sc.nextLine().strip().replace("\"", "");
        if (StrUtil.isBlank(input)) return;

        File target = new File(input);
        if (!target.exists()) {
            Console.error(render("路径不存在：{}", "red"), target.getAbsolutePath());
            return;
        }

        List<File> files = collect(target);
        if (files.isEmpty()) {
            Console.error(render("没有找到可校对的 TXT/HTML 文件：{}", "red"), target.getAbsolutePath());
            return;
        }
        printPlan(target, files);

        if (config.getAi().usable()) {
            Console.print(render("==> 将向 {} 发送正文进行校对，输入 Y 确认: ", "green"), config.getAi().getBaseUrl());
            if (!"Y".equalsIgnoreCase(sc.next().strip())) {
                Console.log("<== 已取消");
                return;
            }
        } else {
            Console.log(render("! 未配置完整 AI 服务，本次仅执行确定性乱码修复（不消耗 token）", "yellow"));
        }

        run(target, files);
    }

    /**
     * 非交互入口（CLI --proofread）：显式传入路径即视为用户同意
     *
     * @return 统计摘要
     */
    public String run(File target) {
        if (!target.exists()) {
            throw new IllegalArgumentException("路径不存在：" + target.getAbsolutePath());
        }
        List<File> files = collect(target);
        if (files.isEmpty()) {
            Console.error(render("没有找到可校对的 TXT/HTML 文件：{}", "red"), target.getAbsolutePath());
            return null;
        }
        printPlan(target, files);
        return run(target, files);
    }

    private String run(File target, List<File> files) {
        String[] meta = parseName(target.isFile() ? FileUtil.mainName(target) : target.getName());
        return new ProofreadService(config).proofreadFiles(files, meta[0], meta[1],
                headIntro(files.get(0)), logDir(target), true);
    }

    private void printPlan(File target, List<File> files) {
        String[] meta = parseName(target.isFile() ? FileUtil.mainName(target) : target.getName());
        Console.log("<== 待校对 {} 个文件，书名《{}》，作者 {}", files.size(), meta[0], meta[1]);
        Console.log(render("! 校对结果会就地覆盖文件（自动备份 .proofread.bak），并生成「校对记录.log」", "yellow"));
    }

    private File logDir(File target) {
        if (target.isFile()) {
            return target.getParentFile() == null ? new File(".") : target.getParentFile();
        }
        return target;
    }

    /**
     * 收集待校对文件：单文件直接处理；目录取其下的 TXT/HTML
     */
    private List<File> collect(File target) {
        List<File> files = new ArrayList<>();
        if (target.isFile()) {
            files.add(target);
            return files;
        }
        File[] arr = target.listFiles();
        if (arr == null) return files;
        for (File f : arr) {
            if (!f.isFile()) continue;
            String ext = FileUtil.extName(f.getName()).toLowerCase();
            if (ext.equals("txt") || ext.equals("html")) {
                files.add(f);
            }
        }
        files.sort((a, b) -> a.getName().compareToIgnoreCase(b.getName()));
        return files;
    }

    /**
     * 从文件名解析「书名 (作者)」
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
     * TXT 成品首页为「书名：x / 作者：y / 简介：z」，据此抽取简介作为降级背景
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
