package com.pcdd.sonovel.web;

import cn.hutool.core.lang.Console;
import com.pcdd.sonovel.core.Crawler;
import com.pcdd.sonovel.model.AppConfig;
import com.pcdd.sonovel.model.Chapter;
import com.pcdd.sonovel.model.SearchResult;
import com.pcdd.sonovel.parse.TocParser;
import com.pcdd.sonovel.util.ConfigUtils;

import java.util.List;


public class DownloadFile {
    public void execute(SearchResult sr) {
        AppConfig config = ConfigUtils.defaultConfig();
        List<Chapter> toc;

        config.setSourceId(sr.getSourceId());
        Console.log("<== 正在获取章节目录...");
        TocParser tocParser = new TocParser(config);
        toc = tocParser.parse(sr.getUrl(), 1, Integer.MAX_VALUE);
        new Crawler(config).crawl(sr.getUrl(), toc);
    }
}
