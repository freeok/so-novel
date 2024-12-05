package com.pcdd.sonovel.handle;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Console;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.pcdd.sonovel.model.Book;
import com.pcdd.sonovel.util.FileUtils;
import io.documentnode.epub4j.domain.Author;
import io.documentnode.epub4j.domain.Resource;
import io.documentnode.epub4j.epub.EpubWriter;
import lombok.SneakyThrows;

import java.io.File;
import java.io.FileOutputStream;

import static org.fusesource.jansi.AnsiRenderer.render;

/**
 * @author pcdd
 * Created at 2024/12/4
 */
public class EpubMergeHandler implements PostProcessingHandler {

    @SneakyThrows
    @Override
    public void handle(Book b, File saveDir) {
        // 等待文件系统更新索引
        int attempts = 10;
        while (FileUtil.isDirEmpty(saveDir) && attempts > 0) {
            Thread.sleep(100);
            attempts--;
        }

        if (FileUtil.isDirEmpty(saveDir)) {
            Console.error(render("==> @|red 《{}》（{}）下载章节数为 0，取消生成 EPUB|@"), b.getBookName(), b.getAuthor());
            return;
        }

        io.documentnode.epub4j.domain.Book book = new io.documentnode.epub4j.domain.Book();
        book.getMetadata().addTitle(b.getBookName());
        book.getMetadata().addAuthor(new Author(b.getAuthor()));
        book.getMetadata().addDescription(b.getIntro());
        // 不设置会导致 Apple Books 无法使用苹方字体
        book.getMetadata().setLanguage("zh");

        int i = 1;
        // 遍历下载后的目录，添加章节
        for (File file : FileUtils.sortFilesByName(saveDir)) {
            // 截取第一个 _ 后的字符串，即章节名
            String title = StrUtil.subAfter(FileUtil.mainName(file), "_", false);

            Resource resource = new Resource(FileUtil.readBytes(file), i + ".html");
            resource.setId(String.valueOf(i));
            resource.setTitle(title);
            book.addSection(title, resource);

            i++;
        }

        // Guide guide = book.getGuide();

        // 下载封面失败会导致生成 epub 中断
        try {
            Console.log("<== 正在下载封面：{}", b.getCoverUrl());
            byte[] bytes = HttpUtil.downloadBytes(b.getCoverUrl());
            book.setCoverImage(new Resource(bytes, "cover.jpg"));
        } catch (Exception e) {
            Console.error(render("@|red 封面下载失败：{}|@"), e.getMessage());
        }

        EpubWriter epubWriter = new EpubWriter();
        String savePath = StrUtil.format("{}/{}.epub", saveDir.getParent(), b.getBookName());
        epubWriter.write(book, new FileOutputStream(savePath));
        FileUtil.del(saveDir);
    }

}