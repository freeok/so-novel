package com.pcdd.sonovel.handle;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileAppender;
import cn.hutool.core.lang.Console;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HtmlUtil;
import cn.hutool.http.HttpUtil;
import com.pcdd.sonovel.model.AppConfig;
import com.pcdd.sonovel.model.Book;
import com.pcdd.sonovel.util.FileUtils;
import lombok.AllArgsConstructor;

import java.io.File;
import java.util.List;

/**
 * @author pcdd
 * Created at 2024/12/4
 */
@AllArgsConstructor
public class TxtMergeHandler implements PostProcessingHandler {

    private final AppConfig config;

    @Override
    public void handle(Book book, File saveDir) {
        String outputPath = StrUtil.format("{}{}({}).txt",
                config.getDownloadPath() + File.separator, book.getBookName(), book.getAuthor());
        // 删除旧的同名 txt 文件
        FileUtil.del(outputPath);

        File outputFile = FileUtil.isAbsolutePath(outputPath)
                ? FileUtil.touch(outputPath)
                : FileUtil.touch(System.getProperty("user.dir"), outputPath);
        FileAppender appender = new FileAppender(outputFile, 16, true);
        List<String> info = List.of(
                StrUtil.format("书名：{}", book.getBookName()),
                StrUtil.format("作者：{}", book.getAuthor()),
                StrUtil.format("简介：{}\n", StrUtil.isEmpty(book.getIntro()) ? "暂无" : HtmlUtil.cleanHtmlTag(book.getIntro()))
        );
        // 首页添加书籍信息
        info.forEach(appender::append);

        for (File f : FileUtils.sortFilesByName(saveDir)) {
            appender.append(FileUtil.readUtf8String(f));
        }
        appender.flush();

        Console.log("<== 正在下载封面：{}", book.getCoverUrl());
        File coverFile = HttpUtil.downloadFileFromUrl(book.getCoverUrl(), saveDir);
        FileUtil.rename(coverFile, "0_封面." + FileUtil.getType(coverFile), true);
    }

}