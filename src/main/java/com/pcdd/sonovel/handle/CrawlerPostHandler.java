package com.pcdd.sonovel.handle;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Console;
import cn.hutool.core.util.StrUtil;
import com.pcdd.sonovel.context.BookContext;
import com.pcdd.sonovel.model.AppConfig;
import com.pcdd.sonovel.model.Book;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;

import java.io.File;
import java.util.Set;

/**
 * @author pcdd
 * Created at 2024/3/17
 */
@AllArgsConstructor
public class CrawlerPostHandler {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("txt", "epub", "pdf");
    private final AppConfig config;

    @SneakyThrows
    public void handle(File saveDir) {
        Book book = BookContext.get();
        String extName = config.getExtName();
        StringBuilder s = new StringBuilder(StrUtil.format("\n<== 《{}》（{}）下载完毕，", book.getBookName(), book.getAuthor()));

        if (ALLOWED_EXTENSIONS.contains(extName.toLowerCase())) {
            s.append("正在合并为 ").append(extName.toUpperCase());
        }
        if ("html".equals(extName)) {
            s.append("正在生成 HTML 目录文件");
        }
        Console.log(s.append("..."));

        // 等待文件系统更新索引
        int attempts = 10;
        while (FileUtil.isDirEmpty(saveDir) && attempts > 0) {
            Thread.sleep(100);
            attempts--;
        }

        PostHandlerFactory.getHandler(extName, config).handle(book, saveDir);

        if (ALLOWED_EXTENSIONS.contains(extName.toLowerCase()) && config.getPreserveChapterCache() == 0) {
            FileUtil.del(saveDir);
        }
    }

}