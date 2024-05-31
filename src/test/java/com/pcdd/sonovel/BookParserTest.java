package com.pcdd.sonovel;

import com.pcdd.sonovel.model.Book;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.pcdd.sonovel.parse.BookParser.replaceCover;

class BookParserTest {

    @Test
    @DisplayName("替换最新封面")
    void test01() {
        Book book = new Book();
        book.setBookName("遮天");
        book.setAuthor("辰东");
        book.setCoverUrl("https://t15.baidu.com/it/u=1106626802,1063170583&fm=224");
        book.setCoverUrl(replaceCover(book));
        System.out.println(book);
    }

}