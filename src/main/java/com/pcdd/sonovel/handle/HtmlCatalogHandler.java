package com.pcdd.sonovel.handle;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileReader;
import cn.hutool.core.io.file.FileWriter;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import com.pcdd.sonovel.model.Book;
import com.pcdd.sonovel.util.FileUtils;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * @author pcdd
 * Created at 2024/12/4
 */
public class HtmlCatalogHandler implements PostProcessingHandler {

    @Override
    public void handle(Book b, File saveDir) {
        List<String> strings = new ArrayList<>();
        List<File> files = FileUtils.sortFilesByName(saveDir);
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

}

