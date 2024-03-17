package com.pcdd.sonovel;

import cn.hutool.core.io.resource.ResourceUtil;
import lombok.SneakyThrows;
import nl.siegmann.epublib.domain.Author;
import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.epub.EpubWriter;
import org.junit.jupiter.api.Test;

import java.io.FileOutputStream;


class EpubTest {

    @Test
    @SneakyThrows
    void createEpub() {
        Book book = new Book();
        book.getMetadata().addTitle("重生之老子是皇帝");
        book.getMetadata().addAuthor(new Author("贰蛋"));
        book.getMetadata().addDescription("醉卧美人膝，醒掌天下权，这才是男人该有的生活！赵洞庭穿越成皇，为这个小目标不断奋斗。");
        book.setCoverImage(new Resource(ResourceUtil.readBytes("cover.jpg"), "cover.jpg"));

        // 循环添加章节
        for (int i = 1; i <= 10; i++) {
            String chapterTitle = String.format("第%03d章", i);
            // 第二个参数必须确保唯一
            Resource resource = new Resource(ResourceUtil.readBytes("chapter.html"), i + ".html");
            book.addSection(chapterTitle, resource);
        }

        EpubWriter epubWriter = new EpubWriter();
        epubWriter.write(book, new FileOutputStream("test.epub"));
    }

}
