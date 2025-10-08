package com.pcdd.sonovel.handle;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileAppender;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HtmlUtil;
import com.pcdd.sonovel.model.AppConfig;
import com.pcdd.sonovel.model.Book;
import com.pcdd.sonovel.util.FileUtils;
import lombok.AllArgsConstructor;

import java.io.File;
import java.nio.charset.Charset;
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

        // 获取 TXT 编码，默认 UTF-8
        Charset charset = CharsetUtil.parse(config.getTxtEncoding());

        FileAppender appender = new FileAppender(outputFile, charset, 16, true);
        // 首页添加书籍信息
        List.of(
                StrUtil.format("书名：{}", book.getBookName()),
                StrUtil.format("作者：{}", book.getAuthor()),
                StrUtil.format("简介：{}\n", StrUtil.isEmpty(book.getIntro()) ? "暂无" : HtmlUtil.cleanHtmlTag(book.getIntro()))
        ).forEach(appender::append);

        for (File f : FileUtils.sortFilesByName(saveDir)) {
            appender.append(FileUtil.readUtf8String(f));
        }
        appender.flush();

        downloadCover(book, saveDir);
    }

}