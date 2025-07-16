package com.pcdd.sonovel.handle;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileReader;
import cn.hutool.core.io.file.FileWriter;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import com.pcdd.sonovel.model.Book;
import com.pcdd.sonovel.util.FileUtils;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @author pcdd
 * Created at 2024/12/4
 */
public class HtmlTocHandler implements PostProcessingHandler {

    @Override
    public void handle(Book book, File saveDir) {
        String regex = "<title>(.*?)</title>";
        List<String> lines = CollUtil.newArrayList("文件名\t\t\t\t章节名");

        for (File file : FileUtils.sortFilesByName(saveDir)) {
            FileReader fr = FileReader.create(file, StandardCharsets.UTF_8);
            String index = StrUtil.subBefore(file.getName(), "_", false);
            String title = ReUtil.getGroup1(regex, fr.readString());
            lines.add(StrUtil.format("{}_.html\t\t{}", index, title));
        }

        File file = FileUtil.touch(saveDir + File.separator, "0_目录.txt");
        FileWriter fw = FileWriter.create(file, StandardCharsets.UTF_8);
        fw.writeLines(lines);

        downloadCover(book, saveDir);
    }

}