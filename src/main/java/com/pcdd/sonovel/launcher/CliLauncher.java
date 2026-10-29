package com.pcdd.sonovel.launcher;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.Console;
import cn.hutool.core.util.StrUtil;
import com.pcdd.sonovel.actions.ProofreadLocalAction;
import com.pcdd.sonovel.core.AppConfigLoader;
import com.pcdd.sonovel.core.Crawler;
import com.pcdd.sonovel.model.AppConfig;
import com.pcdd.sonovel.model.Rule;
import com.pcdd.sonovel.model.Rule.Book;
import com.pcdd.sonovel.model.SearchResult;
import com.pcdd.sonovel.parser.BookParser;
import com.pcdd.sonovel.utils.SourceUtils;
import picocli.CommandLine;

import java.io.File;

import static org.fusesource.jansi.AnsiRenderer.render;

/**
 * 命令行界面 (Command Line Interface)
 *
 * @author pcdd
 * Created at 2025/7/10
 */
@CommandLine.Command(
        name = "SoNovel",
        mixinStandardHelpOptions = true,
        versionProvider = CliLauncher.VersionProvider.class
)
public class CliLauncher implements Runnable {

    @CommandLine.Option(names = {"--url", "-u"}, description = "书籍详情页链接")
    String bookUrl;
    @CommandLine.Option(names = {"--ext", "-e"}, description = "下载格式，可选值：txt|epub|html|pdf，默认 epub", defaultValue = "epub")
    String extName;
    @CommandLine.Option(names = {"--ai"}, description = "下载完成后执行 AI 校对（修正错别字/删除乱码），覆盖 config.ini 的 [ai] enabled")
    boolean aiProofread;
    @CommandLine.Option(names = {"--proofread"}, paramLabel = "PATH",
            description = "只校对本地文件/目录（TXT/HTML），不下载；与 --url 互斥")
    String proofreadPath;

    private static final AppConfig APP_CONFIG = AppConfigLoader.APP_CONFIG;

    /**
     * 全本下载 / 本地校对
     */
    @Override
    public void run() {
        // 分支一：只校对本地已有书籍，不下载
        if (StrUtil.isNotBlank(proofreadPath)) {
            Assert.isTrue(StrUtil.isBlank(bookUrl), "--proofread 与 --url 不能同时使用！");
            // 显式请求校对即视为开启，不要求先改 config.ini
            APP_CONFIG.getAi().setEnabled(1);
            new ProofreadLocalAction(APP_CONFIG).run(new File(proofreadPath));
            return;
        }

        Assert.notEmpty(bookUrl, "书籍详情页链接不能为空！");
        Assert.isTrue(extName.matches("(?i)(txt|epub|html|pdf)"), "下载格式不合法！可选值：txt|epub|html|pdf");

        if (aiProofread) {
            APP_CONFIG.getAi().setEnabled(1);
        }

        Console.log("下载链接: {}，下载格式: {}，AI 校对: {}", bookUrl, extName,
                APP_CONFIG.getAi().getEnabled() == 1 ? "启用" : "关闭");

        Rule rule = SourceUtils.getRule(bookUrl);
        APP_CONFIG.setSourceId(rule.getId());
        APP_CONFIG.setExtName(extName);

        Book book = new BookParser(APP_CONFIG).parse(bookUrl);
        SearchResult sr = SearchResult.builder()
                .url(book.getUrl())
                .bookName(book.getBookName())
                .author(book.getAuthor())
                .latestChapter(book.getLatestChapter())
                .lastUpdateTime(book.getLastUpdateTime())
                .build();
        Console.log("<== {}》({})，正在解析章节目录...", sr.getBookName(), sr.getAuthor());
        new Crawler(APP_CONFIG).crawl(sr.getUrl());
    }

    static class VersionProvider implements CommandLine.IVersionProvider {
        @Override
        public String[] getVersion() {
            return new String[]{render(APP_CONFIG.getVersion(), "green")};
        }
    }

}