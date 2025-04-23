package com.pcdd.sonovel.merge;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileAppender;
import com.pcdd.sonovel.util.FileUtils;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

/**
 * 测试结论：多线程无法提高 I/O 密集型任务的性能（epub 章节的添加、多个 txt 合并）
 *
 * @author pcdd
 * Created at 2024/12/8
 */

class TxtMergerTest {

    // static File dir = FileUtil.file("e:/Temp/small_dir");
    static File dir = FileUtil.file("d:/Temp/small_dir");
    static File outputFile = FileUtil.file("e:/Temp/Merge.txt");

    @RepeatedTest(1)
    @DisplayName("首次执行速度最慢，73 s")
    void test01() {
        FileAppender appender = new FileAppender(outputFile, 16, true);
        for (File item : FileUtils.sortFilesByName(dir)) {
            System.out.println(item.getName());
            appender.append(FileUtil.readUtf8String(item));
        }
        appender.flush();
    }

    @RepeatedTest(1)
    @SneakyThrows
    void test02() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile, StandardCharsets.UTF_8, true))) {
            for (File item : FileUtils.sortFilesByName(dir)) {
                System.out.println(item.getName());
                try (BufferedReader reader = new BufferedReader(new FileReader(item, StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        writer.write(line);
                        writer.newLine();
                    }
                }
                writer.newLine();
            }
        }
    }

    @RepeatedTest(1)
    @SneakyThrows
    @DisplayName("test02 的优化")
    void test03() {
        List<File> files = FileUtils.sortFilesByName(dir);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile, StandardCharsets.UTF_8, true), 16 * 1024)) {
            for (File item : files) {
                System.out.println("Processing: " + item.getName());
                try (BufferedReader reader = new BufferedReader(new FileReader(item, StandardCharsets.UTF_8), 16 * 1024)) {
                    char[] buffer = new char[16 * 1024]; // 缓冲数组
                    int len;
                    while ((len = reader.read(buffer)) != -1) {
                        writer.write(buffer, 0, len);
                    }
                }
                writer.newLine(); // 文件之间添加换行符
            }
        }
    }

    @RepeatedTest(1)
    @DisplayName("NIO 13.905 s")
    @SneakyThrows
    void test04() {
        List<File> files = FileUtils.sortFilesByName(dir);
        Path outputPath = Paths.get(outputFile.getAbsolutePath());
        try (BufferedWriter writer = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            for (File item : files) {
                System.out.println("Processing: " + item.getName());
                Files.lines(item.toPath(), StandardCharsets.UTF_8).forEach(line -> {
                    try {
                        writer.write(line);
                        writer.newLine();
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                });
                writer.newLine();
            }
        }
    }

    @RepeatedTest(1)
    @DisplayName("FileChannel 11.107 s")
    @SneakyThrows
    void test05() {
        List<File> files = FileUtils.sortFilesByName(dir);
        try (FileChannel outChannel = FileChannel.open(Paths.get(outputFile.getAbsolutePath()), StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.APPEND)) {
            for (File file : files) {
                System.out.println("Processing: " + file.getName());
                try (FileChannel inChannel = FileChannel.open(file.toPath(), StandardOpenOption.READ)) {
                    long size = inChannel.size();
                    MappedByteBuffer buffer = inChannel.map(FileChannel.MapMode.READ_ONLY, 0, size);
                    outChannel.write(buffer);
                }
                outChannel.write(ByteBuffer.wrap(System.lineSeparator().getBytes(StandardCharsets.UTF_8))); // 换行符
            }
        }
    }

}