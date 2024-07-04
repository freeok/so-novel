package com.pcdd.sonovel.core;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileAppender;
import cn.hutool.core.io.file.FileReader;
import cn.hutool.core.io.file.FileWriter;
import cn.hutool.core.lang.Console;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.setting.dialect.Props;
import com.pcdd.sonovel.model.Book;
import com.pcdd.sonovel.util.Settings;
import io.documentnode.epub4j.domain.Author;
import io.documentnode.epub4j.domain.Resource;
import io.documentnode.epub4j.epub.EpubWriter;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author pcdd
 */
@UtilityClass
public class CrawlerPostHandler {

    private static final String SAVE_PATH;

    static {
        Props usr = Settings.usr();
        SAVE_PATH = usr.getStr("savePath");
    }

    public void handle(String extName, Book book, File saveDir) {
        StringBuilder s = new StringBuilder(StrUtil.format("\n<== 《{}》（{}）下载完毕，", book.getBookName(), book.getAuthor()));
        if ("txt".equals(extName) || "epub".equals(extName)) {
            s.append("开始合并为 ").append(extName);
        }
        if ("html".equals(extName)) {
            s.append("开始生成目录文件");
        }
        Console.log(s);

        switch (extName) {
            case "txt" -> mergeTxt(saveDir, book.getBookName(), book.getAuthor());
            case "epub" -> convertToEpub(saveDir, book);
            case "html" -> generateCatalog(saveDir);
        }
    }

    @SneakyThrows
    private void convertToEpub(File dir, Book b) {
        io.documentnode.epub4j.domain.Book book = new io.documentnode.epub4j.domain.Book();
        book.getMetadata().addTitle(b.getBookName());
        book.getMetadata().addAuthor(new Author(b.getAuthor()));
        book.getMetadata().addDescription(b.getDescription());
        // 不设置会导致部分阅读器出现问题（例如ibooks无法使用中文字体）
        book.getMetadata().setLanguage("zh");
        byte[] bytes = HttpUtil.downloadBytes(b.getCoverUrl());
        book.setCoverImage(new Resource(bytes, ".jpg"));

        // TODO 创建 guide
        // Guide guide = book.getGuide();

        int i = 1;
        // 遍历下载后的目录，添加章节
        for (File file : files(dir)) {
            // 截取第一个 _ 后的字符串，即章节名
            String title = StrUtil.subAfter(FileUtil.mainName(file), "_", false);

            Resource resource = new Resource(FileUtil.readBytes(file), i + ".html");
            resource.setId(String.valueOf(i));
            resource.setTitle(title);
            book.addSection(title, resource);

            i++;
        }

        EpubWriter epubWriter = new EpubWriter();
        String savePath = StrUtil.format("{}/{}.epub", dir.getParent(), b.getBookName());
        epubWriter.write(book, new FileOutputStream(savePath));
    }

    private void mergeTxt(File dir, String... args) {
        String path = StrUtil.format("{}{}{}（{}）.txt",
                System.getProperty("user.dir") + File.separator, SAVE_PATH + File.separator, args[0], args[1]);
        File file = FileUtil.touch(path);
        FileAppender appender = new FileAppender(file, 16, true);
        appender.append("文件名\t章节名");

        for (File item : files(dir)) {
            String s = FileUtil.readString(item, StandardCharsets.UTF_8);
            appender.append(s);
        }
        appender.flush();
    }

    private static void generateCatalog(File saveDir) {
        List<String> strings = new ArrayList<>();
        List<File> files = files(saveDir);
        String regex = "<title>(.*?)</title>";

        strings.add("文件名\t\t章节名");
        int i = 1;
        for (File file : files) {
            FileReader fr = FileReader.create(file, StandardCharsets.UTF_8);
            // 获取 title 标签内容
            String title = ReUtil.getGroup1(regex, fr.readString());
            strings.add(StrUtil.format("{}_.html\t\t{}", i++, title));
        }

        File file = FileUtil.touch(saveDir + File.separator, "0_目录.txt");
        FileWriter fw = FileWriter.create(file, StandardCharsets.UTF_8);
        fw.writeLines(strings);
    }

    // 文件排序，按文件名升序
    private List<File> files(File dir) {
        return Arrays.stream(dir.listFiles())
                .sorted((o1, o2) -> {
                    String s1 = o1.getName();
                    String s2 = o2.getName();
                    int no1 = Integer.parseInt(s1.substring(0, s1.indexOf("_")));
                    int no2 = Integer.parseInt(s2.substring(0, s2.indexOf("_")));
                    return no1 - no2;
                }).toList();
    }

}
