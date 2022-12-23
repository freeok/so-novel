package work.pcdd.sonovel;

import cn.hutool.core.lang.Console;
import cn.hutool.core.lang.ConsoleTable;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.setting.dialect.Props;
import lombok.SneakyThrows;
import work.pcdd.sonovel.bean.SearchResultLine;
import work.pcdd.sonovel.util.SearchNovelUtils;

import java.util.List;
import java.util.Scanner;

/**
 * @author 1907263405@qq.com
 * @date 2021/6/10 16:18
 */
public class Application {

    @SneakyThrows
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            printHint();
            String keyword = scanner.nextLine().trim();
            if (keyword.length() == 0) {
                Console.log("<== 关键字不能为空");
                continue;
            }
            if ("exit".equals(keyword)) {
                Console.log("<== bye bye ^-^");
                break;
            }

            List<SearchResultLine> results = SearchNovelUtils.search(keyword);
            if (results.isEmpty()) {
                continue;
            }

            ConsoleTable consoleTable = ConsoleTable.create()
                    .addHeader("序号", "书名", "作者", "最新章节", "最后更新时间");
            // 打印搜索结果
            for (int i = 0; i < results.size(); i++) {
                SearchResultLine searchResultLine = results.get(i);
                consoleTable.addBody(String.valueOf(i),
                        searchResultLine.getBookName(),
                        searchResultLine.getAuthor(),
                        searchResultLine.getLatestChapter(),
                        searchResultLine.getLatestUpdate()
                );
            }
            Console.table(consoleTable);

            Console.log("==> 请输入下载序号（首列的数字）");
            int num = scanner.nextInt();
            Console.log("==> 请输起始章(最小为1)和结束章，用空格隔开");
            int start = scanner.nextInt();
            int end = scanner.nextInt();
            double res = SearchNovelUtils.crawl(results, num, start, end);

            Console.log("\n<== 下载完毕，耗时{}s\n", NumberUtil.round(res, 2));
        }

    }

    private static void printHint() {
        Props props = new Props("config.properties");
        Console.table(ConsoleTable.create()
                // 是否转为全角
                .setSBCMode(false)
                .addHeader("so-novel")
                .addHeader("版本：" + props.getStr("version"))
                .addBody("使用须知")
                .addBody("1.下载速度取决于网络和随机时间间隔，若下载失败请多次尝试或更换网络")
                .addBody("2.结束程序请输入exit")
        );
        Console.log("==> 请输入书名或作者：");
    }

}
