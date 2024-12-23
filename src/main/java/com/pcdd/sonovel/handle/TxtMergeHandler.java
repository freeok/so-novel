package com.pcdd.sonovel.handle;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileAppender;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HtmlUtil;
import cn.hutool.http.HttpUtil;
import com.pcdd.sonovel.model.Book;
import com.pcdd.sonovel.model.ConfigBean;
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

    private final ConfigBean config;

    @Override
    public void handle(Book book, File saveDir) {
        String outputPath = StrUtil.format("{}{}{}（{}）.txt",
                System.getProperty("user.dir") + File.separator, config.getDownloadPath() + File.separator,
                book.getBookName(), book.getAuthor());
        File outputFile = FileUtil.touch(outputPath);
        FileAppender appender = new FileAppender(outputFile, 16, true);

        List<String> format = List.of(
                StrUtil.format("书名：{}", book.getBookName()),
                StrUtil.format("作者：{}", book.getAuthor()),
                StrUtil.format("简介：{}", StrUtil.isEmpty(book.getIntro()) ? "暂无" : HtmlUtil.cleanHtmlTag(book.getIntro())),
                StrUtil.format("{}", "\u3000".repeat(2))
        );
        // 首页添加书籍信息
        format.forEach(appender::append);

        for (File f : FileUtils.sortFilesByName(saveDir)) {
            appender.append(FileUtil.readUtf8String(f));
        }
        appender.flush();

        // 下载封面
        File coverFile = HttpUtil.downloadFileFromUrl(book.getCoverUrl(), System.getProperty("user.dir") + File.separator + saveDir);
        FileUtil.rename(coverFile, "0_封面." + FileUtil.getType(coverFile), true);
    }

}