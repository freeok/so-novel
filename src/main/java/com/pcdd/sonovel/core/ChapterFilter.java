package com.pcdd.sonovel.core;

import cn.hutool.http.HtmlUtil;
import lombok.experimental.UtilityClass;

import java.util.List;

/**
 * @author pcdd
 */
@UtilityClass
public class ChapterFilter {

    private final List<String> filterWords = List.of(
            "一秒记住【文学巴士&nbsp;】，精彩无弹窗免费阅读！",
            "(www.xbiquge.la 新笔趣阁)，高速全文字在线阅读！"
    );

    /**
     * 过滤词汇
     */
    public String filter(String content) {
        StringBuilder filteredContent = new StringBuilder(content);

        for (String word : filterWords) {
            int index = filteredContent.indexOf(word);
            while (index != -1) {
                filteredContent.delete(index, index + word.length());
                index = filteredContent.indexOf(word);
            }
        }

        return filterAds(filteredContent.toString());
    }

    /**
     * 过滤广告，仅限书源 1，不同书源广告 html 可能不同
     */
    private String filterAds(String content) {
        return HtmlUtil.removeHtmlTag(content, "div", "p", "script");
    }

}
