package com.pcdd.sonovel.action;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Console;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.pcdd.sonovel.core.Crawler;
import com.pcdd.sonovel.model.AppConfig;
import com.pcdd.sonovel.model.Chapter;
import com.pcdd.sonovel.model.SearchResult;
import com.pcdd.sonovel.parse.TocParser;
import com.pcdd.sonovel.util.ConfigUtils;

import java.util.List;
import java.util.Scanner;

import static org.fusesource.jansi.AnsiRenderer.render;

/**
 * @author pcdd
 * Created at 2025/3/26
 */
public class DownloadAction {

    private final AppConfig config = ConfigUtils.defaultConfig();
    private final Scanner sc = Console.scanner();

    public void execute(List<SearchResult> results) {
        int num;
        int action;
        SearchResult sr;
        List<Chapter> toc;
        // 3. 选择下载章节
        while (true) {
            Console.print(render("==> 请输入下载序号（或输入 0 结束）：", "green"));
            String input = sc.nextLine().strip();
            // 健壮性判断：必须为数字
            try {
                num = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                continue;
            }

            // 没有搜到想要的书，返回
            if (num == 0) break;
            // 健壮性判断：必须为首列的序号
            if (num < 0 || num > results.size()) continue;

            sr = results.get(num - 1);
            config.setSourceId(sr.getSourceId());
            Console.log("<== 正在获取章节目录...");
            TocParser tocParser = new TocParser(config);
            toc = tocParser.parse(sr.getUrl(), 1, Integer.MAX_VALUE);

            Console.log("<== 你选择了《{}》({})，共计 {} 章，书源 {}: {}", sr.getBookName(), sr.getAuthor(), toc.size(), sr.getSourceId(), sr.getUrl());
            Console.log("""
                    0: 重新选择功能
                    1: 下载全本
                    2: 下载指定范围章节
                    3: 下载最新章节
                    4: 重新输入序号""");

            try {
                Console.print(render("==> 请选择下载方式：", "green"));
                action = Integer.parseInt(sc.nextLine());
            } catch (NumberFormatException e) {
                continue;
            }

            if (action == 0) return;
            if (action == 2) {
                try {
                    Console.print(render("==> 请输起始章(最小为1)和结束章，用空格隔开：", "green"));
                    String[] split = sc.nextLine().strip().split("\\s+");
                    int start = Integer.parseInt(split[0]) - 1;
                    int end = Integer.parseInt(split[1]);
                    if (start > toc.size() || end > toc.size()) {
                        Console.log(render(StrUtil.format("超出章节范围，该小说共 {} 章", toc.size()), "yellow"));
                        return;
                    }
                    toc = CollUtil.sub(toc, start, end);
                } catch (Exception e) {
                    return;
                }
            }
            if (action == 3) {
                try {
                    Console.print(render("==> 请输入要下载最新章节的数量：", "green"));
                    int i = Integer.parseInt(sc.nextLine());
                    toc = CollUtil.sub(toc, toc.size() - i, toc.size());
                } catch (Exception e) {
                    return;
                }
            }
            if (action == 4) continue;

            double res = new Crawler(config).crawl(sr.getUrl(), toc);
            Console.log(render("<== 完成！总耗时 {} s", "green"), NumberUtil.round(res, 2));
        }
    }

}