package com.pcdd.sonovel.core;

import cn.hutool.http.HtmlUtil;
import lombok.experimental.UtilityClass;

import java.util.List;

/**
 * @author pcdd
 * 规则仅适用于书源 1
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
        content = filterCharacters(content);
        return filterAds(content);
    }

    /**
     * 过滤乱码字符。即除了汉字、中文符号、字母、英文符号、数字之外的，但包括 ASCII 控制字符
     */
    private String filterCharacters(String content) {
        /*  匹配现代汉字                     [\u4e00-\u9fa5]
            匹配更广泛的汉字                  [\u4e00-\u9fff]
            匹配中文标点                     ·【】「」、；’，。！￥…（）—：“”《》？
            匹配 ASCII 字符（字母、数字、符号） [\x00-\x7F]
            匹配 ASCII 控制字符              [\x00-\x1F\x7F]
        */
        return content.replaceAll("[^\\u4e00-\\u9fff·【】「」、；’，。！￥…（）—：“”《》？\\x00-\\x7F]|[\\x00-\\x1F\\x7F]", "");
    }

    /**
     * 过滤广告，仅限书源 1，不同书源广告 html 可能不同
     */
    private String filterAds(String content) {
        StringBuilder filteredContent = new StringBuilder(content);

        for (String word : filterWords) {
            int index = filteredContent.indexOf(word);
            while (index != -1) {
                filteredContent.delete(index, index + word.length());
                index = filteredContent.indexOf(word);
            }
        }

        return HtmlUtil.removeHtmlTag(filteredContent.toString(), "div", "p", "script");
    }

}
