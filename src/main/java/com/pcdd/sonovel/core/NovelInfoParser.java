package com.pcdd.sonovel.core;

import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONUtil;
import com.pcdd.sonovel.model.NovelInfo;
import com.pcdd.sonovel.model.Rule;
import lombok.SneakyThrows;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * @author pcdd
 */
public class NovelInfoParser {

    private final Rule rule;

    public NovelInfoParser(int sourceId) {
        // 根据 ruleId 获取对应 json 文件内容
        String jsonStr = FileUtil.readString("rule/rule" + sourceId + ".json", StandardCharsets.UTF_8);
        // json 封装进 Rule
        this.rule = JSONUtil.toBean(jsonStr, Rule.class);
    }

    @SneakyThrows
    public NovelInfo parse(String url) {
        Rule.Book r = rule.getBook();
        Document document = Jsoup.parse(new URL(url), 10000);
        String bookName = document.selectXpath(r.getBookName()).text();
        String author = document.selectXpath(r.getAuthor()).attr("content");
        String description = document.selectXpath(r.getDescription()).text();
        String coverUrl = document.selectXpath(r.getCoverUrl()).attr("src");

        NovelInfo novelInfo = new NovelInfo();
        novelInfo.setUrl(url);
        novelInfo.setBookName(bookName);
        novelInfo.setAuthor(author);
        novelInfo.setDescription(description);
        novelInfo.setCoverUrl(coverUrl);

        return novelInfo;
    }

}
