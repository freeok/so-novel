package com.pcdd.sonovel.handle;

import com.pcdd.sonovel.model.Book;

import java.io.File;

/**
 * @author pcdd
 * Created at 2024/12/4
 */
public interface PostProcessingHandler {

    void handle(Book book, File saveDir);

}

