package com.pcdd.sonovel.handle;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileAppender;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HtmlUtil;
import com.pcdd.sonovel.model.Book;
import com.pcdd.sonovel.model.ConfigBean;
import com.pcdd.sonovel.util.FileUtils;
import lombok.AllArgsConstructor;

import java.io.File;
import java.nio.charset.StandardCharsets;

/**
 * @author pcdd
 * Created at 2024/12/4
 */
@AllArgsConstructor
public class TxtMergeHandler implements PostProcessingHandler {

    private final ConfigBean config;

    @Override
    public void handle(Book b, File saveDir) {
        String path = StrUtil.format("{}{}{}（{}）.txt",
                System.getProperty("user.dir") + File.separator, config.getDownloadPath() + File.separator,
                b.getBookName(), b.getAuthor());
        File file = FileUtil.touch(path);
        FileAppender appender = new FileAppender(file, 16, true);

        // 添加书名和作者信息
        appender.append(StrUtil.format("书名：{}\n作者：{}\n简介：\n{}\n",
                b.getBookName(), b.getAuthor(), "\u3000".repeat(2) + HtmlUtil.cleanHtmlTag(b.getIntro())));

        for (File item : FileUtils.sortFilesByName(saveDir)) {
            String content = FileUtil.readString(item, StandardCharsets.UTF_8);
            appender.append(content);
        }

        appender.flush();
    }
}
