package com.pcdd.sonovel.handle;

import cn.hutool.core.lang.Console;
import cn.hutool.core.util.StrUtil;
import com.pcdd.sonovel.model.AppConfig;
import com.pcdd.sonovel.model.Book;
import lombok.AllArgsConstructor;

import java.io.File;

/**
 * @author pcdd
 * Created at 2024/3/17
 */
@AllArgsConstructor
public class CrawlerPostHandler {

    private final AppConfig config;

    public void handle(Book book, File saveDir) {
        String extName = config.getExtName();
        StringBuilder s = new StringBuilder(StrUtil.format("\n<== 《{}》（{}）下载完毕，", book.getBookName(), book.getAuthor()));

        if (extName.matches("txt|epub")) {
            s.append("正在合并为 ").append(extName.toUpperCase());
        }
        if ("html".equals(extName)) {
            s.append("正在生成 HTML 目录文件");
        }
        Console.log(s.append(" ..."));

        PostHandlerFactory.getHandler(extName, config).handle(book, saveDir);
    }

}