package com.pcdd.sonovel;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.pcdd.sonovel.core.ChapterFilter;
import com.pcdd.sonovel.model.SearchResult;
import lombok.SneakyThrows;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

class CrawlTest {

    @Test
    @DisplayName("解析搜索结果页")
    @SneakyThrows
    void test01() {
        Connection connect = Jsoup.connect("https://www.xbiqugu.info/modules/article/waps.php");
        // 搜索结果页DOM
        Document document = connect.data("searchkey", "斗罗大陆").post();
        Elements elements = document.selectXpath("//*[@id=\"checkform\"]/table/tbody/tr");

        for (Element element : elements) {
            String url = element.selectXpath("td[@class='even']/a").attr("href");
            String bookName = element.selectXpath("td[@class='even']/a").text();
            String latestChapter = element.selectXpath("td[@class='odd']/a").text();
            String author = element.selectXpath("td[@class='even'][2]").text();
            String update = element.selectXpath("td[@class='odd'][2]").text();

            SearchResult build = SearchResult.builder().url(url).bookName(bookName).latestChapter(latestChapter).author(author).latestUpdate(update).build();
            System.out.println(build);
        }
    }

    @Test
    @DisplayName("爬取章节并下载")
    @SneakyThrows
    void test02() {
        Document document = Jsoup.parse(new URL("https://www.xbiqugu.info/116/116314/43917573.html"), 30_000);
        String title = document.selectXpath("//*[@class='bookname']/h1").text();
        System.out.println(title);
        String content = document.getElementById("content").html();
        content = ChapterFilter.filter(content);
        content = "<br>".concat(content.replaceAll("&nbsp;|\\s+", ""))
                .replaceAll("<br>(.*?)<br>", "<p>$1</p>")
                .replaceAll("<p></p>|<br>", "");

        String path = StrUtil.format("{}.html", title);
        try (OutputStream fos = new BufferedOutputStream(new FileOutputStream(path))) {
            fos.write(content.getBytes(StandardCharsets.UTF_8));
        }
    }

    // @Test
    @DisplayName("目录排序")
    void test03() {
        File dir = FileUtil.file("D:\\Code\\IdeaProjects\\so-novel\\download\\斗罗大陆（唐家三少）");
        List<File> files = Arrays.stream(dir.listFiles())
                .sorted((o1, o2) -> {
                    String s1 = o1.getName();
                    String s2 = o2.getName();
                    int no1 = Integer.parseInt(s1.substring(0, s1.indexOf("_")));
                    int no2 = Integer.parseInt(s2.substring(0, s2.indexOf("_")));
                    return no1 - no2;
                })
                .toList();
        for (File item : files) {
            System.out.println(item.getName());
        }
    }

}
