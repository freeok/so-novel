package com.pcdd.sonovel.core;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileAppender;
import cn.hutool.core.io.file.FileReader;
import cn.hutool.core.io.file.FileWriter;
import cn.hutool.core.lang.Console;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.Header;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.pcdd.sonovel.model.Book;
import com.pcdd.sonovel.model.ConfigBean;
import com.pcdd.sonovel.util.RandomUA;
import io.documentnode.epub4j.domain.Author;
import io.documentnode.epub4j.domain.Resource;
import io.documentnode.epub4j.epub.EpubWriter;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.fusesource.jansi.AnsiRenderer.render;

/**
 * @author pcdd
 */
@AllArgsConstructor
public class CrawlerPostHandler {

    private final ConfigBean config;
    public static final int TIMEOUT_MILLS = 10_000;

    public void handle(Book book, File saveDir) {
        String extName = config.getExtName();
        StringBuilder s = new StringBuilder(StrUtil.format("\n<== 《{}》（{}）下载完毕，", book.getBookName(), book.getAuthor()));

        if ("txt".equals(extName) || "epub".equals(extName)) {
            s.append("正在合并为 ").append(extName.toUpperCase());
        }
        if ("html".equals(extName)) {
            s.append("正在生成 HTML 目录文件");
        }
        Console.log(s.append(" ..."));

        switch (extName) {
            case "epub" -> convert2Epub(saveDir, book);
            case "txt" -> mergeTxt(saveDir, book.getBookName(), book.getAuthor());
            case "html" -> generateCatalog(saveDir);
            default -> Console.error("暂不支持的格式：{}", extName);
        }
    }

    @SneakyThrows
    private void convert2Epub(File dir, Book b) {
        // 等待文件系统更新索引
        Thread.sleep(500);
        if (FileUtil.isDirEmpty(dir)) {
            Console.error(render("==> @|red 《{}》（{}）下载章节数为 0，取消生成 EPUB|@"), b.getBookName(), b.getAuthor());
            return;
        }

        io.documentnode.epub4j.domain.Book book = new io.documentnode.epub4j.domain.Book();
        book.getMetadata().addTitle(b.getBookName());
        book.getMetadata().addAuthor(new Author(b.getAuthor()));
        book.getMetadata().addDescription(b.getIntro());
        // 不设置会导致 Apple Books 无法使用苹方字体
        book.getMetadata().setLanguage("zh");

        // 下载封面失败会导致生成 epub 中断
        try (HttpResponse resp = HttpUtil.createGet(b.getCoverUrl())
                .timeout(TIMEOUT_MILLS)
                .header(Header.USER_AGENT, RandomUA.generate())
                .execute()) {
            byte[] bytes = resp.bodyBytes();
            book.setCoverImage(new Resource(bytes, ".jpg"));
        } catch (Exception e) {
            Console.error("封面下载失败：{}", e.getMessage());
        }

        // Guide guide = book.getGuide();

        int i = 1;
        // 遍历下载后的目录，添加章节
        for (File file : sortFilesByName(dir)) {
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
                System.getProperty("user.dir") + File.separator, config.getDownloadPath() + File.separator, args[0], args[1]);
        File file = FileUtil.touch(path);
        FileAppender appender = new FileAppender(file, 16, true);

        for (File item : sortFilesByName(dir)) {
            String s = FileUtil.readString(item, StandardCharsets.UTF_8);
            appender.append(s);
        }
        appender.flush();
    }

    private void generateCatalog(File saveDir) {
        List<String> strings = new ArrayList<>();
        List<File> files = sortFilesByName(saveDir);
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
    private List<File> sortFilesByName(File dir) {
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
