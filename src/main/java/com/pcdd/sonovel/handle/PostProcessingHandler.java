package com.pcdd.sonovel.handle;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Console;
import cn.hutool.http.HttpUtil;
import com.pcdd.sonovel.model.Book;
import com.pcdd.sonovel.util.FileUtils;

import java.io.File;

import static org.fusesource.jansi.AnsiRenderer.render;

/**
 * @author pcdd
 * Created at 2024/12/4
 */
public interface PostProcessingHandler {

    void handle(Book book, File saveDir);

    /**
     * 下载封面失败会导致生成中断，必须捕获异常
     */
    default void downloadCover(Book book, File saveDir) {
        try {
            File coverFile = HttpUtil.downloadFileFromUrl(book.getCoverUrl(), FileUtils.resolvePath(saveDir.toString()));
            FileUtil.rename(coverFile, "0_封面." + FileUtil.getType(coverFile), true);
        } catch (Exception e) {
            Console.error(render("封面 {} 下载失败：{}", "red"), book.getCoverUrl(), e.getMessage());
        }
    }

}