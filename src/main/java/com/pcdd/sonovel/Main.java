package com.pcdd.sonovel;

import cn.hutool.core.lang.Console;
import cn.hutool.core.lang.ConsoleTable;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.setting.dialect.Props;
import com.pcdd.sonovel.core.Crawler;
import com.pcdd.sonovel.model.SearchResult;
import com.pcdd.sonovel.util.Settings;
import lombok.SneakyThrows;

import java.util.List;
import java.util.Scanner;

import static org.fusesource.jansi.AnsiRenderer.render;

/**
 * @author pcdd
 * Created at 2021/6/10 16:18
 */
public class Main {

    @SneakyThrows
    public static void main(String[] args) {
        Scanner scanner = Console.scanner();
        printHint();

        while (true) {
            Console.log(render("==> @|blue 请输入书名或作者：|@"));
            // 1. 输入书名或作者
            String keyword = scanner.nextLine().trim();
            if (keyword.isEmpty()) {
                continue;
            }
            if ("exit".equals(keyword.toLowerCase().trim())) {
                Console.log("<== bye :)");
                break;
            }
            List<SearchResult> results = Crawler.search(keyword);
            if (results.isEmpty()) {
                continue;
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

    }

    private static void printHint() {
        Props sys = Settings.sys();
        Props usr = Settings.usr();
        Console.table(ConsoleTable.create()
                // 是否转为全角
                .setSBCMode(false)
                .addHeader(render(StrUtil.format("@|BG_blue,ITALIC,BOLD  so-novel v{} |@", sys.getStr("version"))) + "（本项目开源且免费）")
                .addHeader("当前书源：" + sys.getStr("index_url"))
                .addHeader(render("导出格式：@|blue " + usr.getStr("extName") + "|@"))
                .addBody("使用须知")
                .addBody("1. 下载受书源、网络等因素影响，若下载失败可尝试增大爬取间隔")
                .addBody("2. 请按要求输入，然后按回车（Enter）执行")
                .addBody("3. 可输入 exit 结束程序")
        );
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
