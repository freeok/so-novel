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
        book.setBookName("星辰大道");
        book.setAuthor("随散飘风");
        System.out.println(CoverUpdater.fetchCover(book, null));
    }

}