package com.pcdd.sonovel.action;

import cn.hutool.core.lang.Console;
import cn.hutool.core.lang.ConsoleTable;
import cn.hutool.core.util.NumberUtil;
import com.pcdd.sonovel.core.Crawler;
import com.pcdd.sonovel.model.ConfigBean;
import com.pcdd.sonovel.model.SearchResult;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;

import java.util.List;

import static org.fusesource.jansi.AnsiRenderer.render;

/**
 * @author pcdd
 */
@AllArgsConstructor
public class DownloadAction {

    private final ConfigBean config;

    @SneakyThrows
    public void execute(Terminal terminal) {
        LineReader reader = LineReaderBuilder.builder().terminal(terminal).build();
        // 1. 查询
        String keyword = reader.readLine(render("==> @|blue 请输入书名或作者（宁少字别错字）: |@")).trim();
        if (keyword.isEmpty()) return;
        List<SearchResult> results = new Crawler(config).search(keyword);
        if (results.isEmpty()) return;

        // 2. 打印搜索结果
        printSearchResult(results);

        int num = 0;
        int downloadPolicy = 2;
        // 3. 选择下载章节
        do {
            String input = reader.readLine("==> 请输入下载序号（首列的数字）").trim();
            // 健壮性判断：必须为数字
            try {
                num = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                continue;
            }
            // 健壮性判断：必须为首列的数字
            if (num < 0 || num >= results.size()) continue;
            SearchResult sr = results.get(num);
            Console.log("<== 你选择了《{}》({})", sr.getBookName(), sr.getAuthor());
            Console.log("==> 0: 下载全本");
            Console.log("==> 1: 下载指定章节");
            Console.log("==> 2: 重新选择");
            downloadPolicy = Integer.parseInt(reader.readLine("==> 请输入数字："));
        } while (downloadPolicy == 2);

        int start = 1;
        int end = Integer.MAX_VALUE;
        if (downloadPolicy == 1) {
            String[] split = reader.readLine("==> 请输起始章(最小为1)和结束章，用空格隔开：").trim().split("\\s+");
            start = Integer.parseInt(split[0]);
            end = Integer.parseInt(split[1]);
        }
        double res = new Crawler(config).crawl(results, num, start, end);
        Console.log("<== 完成！总耗时 {} s\n", NumberUtil.round(res, 2));
    }

    private static void printSearchResult(List<SearchResult> results) {
        ConsoleTable consoleTable = ConsoleTable.create().addHeader("序号", "书名", "作者", "最新章节", "最后更新时间");
        for (int i = 0; i < results.size(); i++) {
            SearchResult r = results.get(i);
            consoleTable.addBody(String.valueOf(i),
                    r.getBookName(),
                    r.getAuthor(),
                    r.getLatestChapter(),
                    r.getLatestUpdate());
        }
        Console.table(consoleTable);
    }

}
