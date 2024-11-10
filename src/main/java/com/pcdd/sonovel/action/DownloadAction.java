package com.pcdd.sonovel.action;

import cn.hutool.core.lang.Console;
import cn.hutool.core.lang.ConsoleTable;
import cn.hutool.core.util.NumberUtil;
import com.pcdd.sonovel.core.Crawler;
import com.pcdd.sonovel.model.SearchResult;
import org.jline.terminal.Terminal;

import java.util.List;
import java.util.Scanner;

import static org.fusesource.jansi.AnsiRenderer.render;

public class DownloadAction implements Action {

    @Override
    public void execute(Terminal terminal) {
        Scanner scanner = Console.scanner();
        Console.log(render("==> @|blue 请输入书名或作者：|@"));
        // 1. 查询
        String keyword = scanner.nextLine().trim();
        if (keyword.isEmpty()) {
            return;
        }
        List<SearchResult> results = Crawler.search(keyword);
        if (results.isEmpty()) {
            return;
        }
        // 2. 打印搜索结果
        printSearchResult(results);

        // 3. 选择后下载
        Console.log("==> 请输入下载序号（首列的数字）");
        int num = Integer.parseInt(scanner.nextLine());
        Console.log("==> 0: 下载全本");
        Console.log("==> 1: 下载指定章节");
        int downloadPolicy = Integer.parseInt(scanner.nextLine());
        int start = 1;
        int end = Integer.MAX_VALUE;
        if (downloadPolicy == 1) {
            Console.log("==> 请输起始章(最小为1)和结束章，用空格隔开");
            String[] split = scanner.nextLine().split("\\s+");
            start = Integer.parseInt(split[0]);
            end = Integer.parseInt(split[1]);
        }
        double res = Crawler.crawl(results, num, start, end);
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
