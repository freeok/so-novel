package com.pcdd.sonovel;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.lang.Console;
import com.pcdd.sonovel.core.AppConfigLoader;
import com.pcdd.sonovel.core.ChapterRenderer;
import com.pcdd.sonovel.model.AppConfig;
import io.documentnode.epub4j.domain.*;
import io.documentnode.epub4j.epub.EpubReader;
import io.documentnode.epub4j.epub.EpubWriter;
import lombok.SneakyThrows;
import org.junit.jupiter.api.*;

import java.nio.file.Path;


@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class Epub4jTest {

    static final AppConfig APP_CONFIG = AppConfigLoader.APP_CONFIG;

    @Test
    @Order(1)
    @DisplayName("创建 EPUB")
    @SneakyThrows
    void createEpub() {
        Book book = new Book();
        book.getMetadata().addTitle("穿越成皇，重生为君");
        book.getMetadata().addAuthor(new Author("贰蛋"));
        book.getMetadata().addDescription("南宋景炎三年，雷州府侧碙州岛。古色古香的房间，雕龙刻凤的床榻。可此时，却是有声凄厉如夜啼的哭声响起，“皇上驾崩了”");
        book.getMetadata().addType("历史");
        book.getMetadata().addPublisher("黑岩网");
        book.getMetadata().setLanguage(APP_CONFIG.getLanguage());
        book.setCoverImage(new Resource(ResourceUtil.readBytes("cover.jpg"), "cover.jpg"));

        ChapterRenderer renderer = new ChapterRenderer(APP_CONFIG);
        // 循环添加章节
        for (int i = 1; i <= 10; i++) {
            String title = String.format("第%03d章", i);
            String content = renderer.renderTemplateFormat(title, "正文内容 " + i, "epub");
            // 第二个参数必须确保唯一
            book.addSection(title, new Resource(content.getBytes(), i + ".html"));
        }

        try (var out = FileUtil.getOutputStream(Path.of("target", "test.epub").toFile())) {
            new EpubWriter().write(book, out);
        }
    }

    @Test
    @Order(2)
    @DisplayName("读取 EPUB")
    @SneakyThrows
    void readEpub() {
        EpubReader epubReader = new EpubReader();
        Book book = epubReader.readEpub(FileUtil.getInputStream("test.epub"));
        Metadata metadata = book.getMetadata();

        Console.log("书名：{}", metadata.getTitles().getFirst());
        Console.log("作者：{}", metadata.getAuthors().getFirst());
        Console.log("简介：{}", metadata.getDescriptions().getFirst());
        Console.log("章节数：{}", book.getContents().size());
        Console.log("类型：{}", book.getMetadata().getTypes());
        Console.log("出版商：{}", book.getMetadata().getPublishers());

        int i = 0;
        // 遍历目录
        for (TOCReference tocReference : book.getTableOfContents().getTocReferences()) {
            if (i++ == 10) break;
            Console.log("章节名：{}", tocReference.getTitle());
            Console.log("正文：{}", new String(tocReference.getResource().getData()));
            Console.log("-".repeat(100));
        }
    }

}