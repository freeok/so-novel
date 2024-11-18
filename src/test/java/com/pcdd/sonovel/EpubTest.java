package com.pcdd.sonovel;

import cn.hutool.core.io.resource.ResourceUtil;
import io.documentnode.epub4j.domain.Author;
import io.documentnode.epub4j.domain.Book;
import io.documentnode.epub4j.domain.Resource;
import io.documentnode.epub4j.epub.EpubWriter;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.io.FileOutputStream;


class EpubTest {

    @Test
    @SneakyThrows
    void createEpub() {
        Book book = new Book();
        book.getMetadata().addTitle("武神主宰");
        book.getMetadata().addAuthor(new Author("暗魔师"));
        book.getMetadata().addDescription("天武大陆一代传奇秦尘，因好友背叛意外陨落武域。三百年后，他转生在一个受尽欺凌的王府私生子身上，利用前世造诣，凝神功、炼神丹，逆天而上，强势崛起，从此踏上一段震惊大陆的惊世之旅。");
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
