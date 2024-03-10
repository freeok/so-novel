package com.pcdd.sonovel;

import cn.hutool.core.io.FileUtil;
import cn.hutool.http.HtmlUtil;
import com.pcdd.sonovel.model.SearchResult;
import lombok.SneakyThrows;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
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

    /**
     * 解析搜索结果页
     */
    @Test
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
    @SneakyThrows
    void test02() {
        Document document = Jsoup.parse(new URL("https://www.xbiqugu.info/116/116314/43917573.html"), 10000);

        String titile = "第1章 逆天悟性，觉醒霸王色";
        String content = document.getElementById("content").html();
        content = HtmlUtil.cleanHtmlTag(content)
                .replace("&nbsp;", " ")
                .replace("最新网址：www.xbiqugu.info", "")
                .replace("亲,点击进去,给个好评呗,分数越高更新越快,据说给香书小说打满分的最后都找到了漂亮的老婆哦!", "")
                .replace("手机站全新改版升级地址：https://wap.xbiqugu.info，数据和书签与电脑站同步，无广告清新阅读！", "");
        // 4 空格
        content = titile + content;
        System.out.println(content);


        // String path = UUID.fastUUID() + ".txt";
        String path = "1.txt";
        try (OutputStream fos = new BufferedOutputStream(new FileOutputStream(path))) {
            fos.write(content.getBytes(StandardCharsets.UTF_8));
        }
    }

    @Test
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

    @Test
    void test04() {
        String s = "\\a/b?c*d<e>f";
        // Windows 文件名非法字符替换
        System.out.println(s.replaceAll("\\\\|/|:|\\*|\\?|<|>", ""));
    }

}
