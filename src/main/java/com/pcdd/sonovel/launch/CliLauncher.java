package com.pcdd.sonovel.launch;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.Console;
import com.pcdd.sonovel.core.AppConfigLoader;
import com.pcdd.sonovel.core.Crawler;
import com.pcdd.sonovel.model.AppConfig;
import com.pcdd.sonovel.model.Book;
import com.pcdd.sonovel.model.Rule;
import com.pcdd.sonovel.model.SearchResult;
import com.pcdd.sonovel.parse.BookParser;
import com.pcdd.sonovel.util.SourceUtils;
import picocli.CommandLine;

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

    private static final AppConfig APP_CONFIG = AppConfigLoader.APP_CONFIG;

    /**
     * 全本下载
     */
    @Override
    public void run() {
        Assert.notEmpty(bookUrl, "书籍详情页链接不能为空！");
        Assert.isTrue(extName.matches("(?i)(txt|epub|html|pdf)"), "下载格式不合法！可选值：txt|epub|html|pdf");

        Console.log("下载链接: {}，下载格式: {}", bookUrl, extName);

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
        Console.log("<== {}》({})，正在解析目录...", sr.getBookName(), sr.getAuthor());
        new Crawler(APP_CONFIG).crawl(sr.getUrl());
    }

    static class VersionProvider implements CommandLine.IVersionProvider {
        @Override
        public String[] getVersion() {
            return new String[]{render(APP_CONFIG.getVersion(), "green")};
        }
    }

}