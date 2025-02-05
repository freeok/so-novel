package com.pcdd.sonovel;

import com.pcdd.sonovel.core.CoverUpdater;
import com.pcdd.sonovel.model.Book;
import org.junit.jupiter.api.Test;

/**
 * @author pcdd
 * Created at 2025/2/6
 */
class CoverUpdaterTest {

    @Test
    void test01() {
        Book book = new Book();
        book.setBookName("从遮天开始穿越");
        book.setAuthor("心之火");
        System.out.println(CoverUpdater.fetchZongheng(book));
    }

}