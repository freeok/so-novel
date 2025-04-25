package com.pcdd.sonovel.handle;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Console;
import cn.hutool.core.util.StrUtil;
import com.pcdd.sonovel.model.AppConfig;
import com.pcdd.sonovel.model.Book;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;

import java.io.File;

/**
 * @author pcdd
 * Created at 2024/3/17
 */
@AllArgsConstructor
public class CrawlerPostHandler {

    private final AppConfig config;

    @SneakyThrows
    public void handle(Book book, File saveDir) {
        String extName = config.getExtName();
        StringBuilder s = new StringBuilder(StrUtil.format("\n<== 《{}》（{}）下载完毕，", book.getBookName(), book.getAuthor()));

        if (extName.matches("(?i)^(txt|epub|pdf)$")) {
            s.append("正在合并为 ").append(extName.toUpperCase());
        }
        if ("html".equals(extName)) {
            s.append("正在生成 HTML 目录文件");
        }
        Console.log(s.append(" ..."));

        // 等待文件系统更新索引
        int attempts = 10;
        while (FileUtil.isDirEmpty(saveDir) && attempts > 0) {
            Thread.sleep(100);
            attempts--;
        }

        PostHandlerFactory.getHandler(extName, config).handle(book, saveDir);
        // FileUtil.del(saveDir);
    }

}