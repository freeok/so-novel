package com.pcdd.sonovel;

import com.pcdd.sonovel.core.CoverUpdater;
import com.pcdd.sonovel.model.Rule.Book;
import org.junit.jupiter.api.Test;

/**
 * @author pcdd
 * Created at 2025/2/6
 */
class CoverUpdaterTest {

    @Test
    void testQidian() {
        Book book = new Book();
        book.setBookName("诡秘之主");
        book.setAuthor("爱潜水的乌贼");
        System.out.println(CoverUpdater.fetchCover(book, null));
    }

    @Test
    void testZongheng() {
        Book book = new Book();
        book.setBookName("剑来");
        book.setAuthor("烽火戏诸侯");
        System.out.println(CoverUpdater.fetchCover(book, null));
    }

    @Test
    void testQimao() {
        Book book = new Book();
        book.setBookName("盖世神医");
        book.setAuthor("狐颜乱语");
        System.out.println(CoverUpdater.fetchCover(book, null));
    }

}