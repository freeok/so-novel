package com.pcdd.sonovel.core;

import cn.hutool.http.HtmlUtil;

/**
 * @author pcdd
 * 规则仅适用于书源 1
 */
public class ChapterFilter extends Source {

    /**
     * 使用正则表达式构建匹配模式
     */
    private final String adsPattern = "一秒记住【文学巴士&nbsp;】，精彩无弹窗免费阅读！|(www.xbiquge.la 新笔趣阁)，高速全文字在线阅读！";

    public ChapterFilter(int sourceId) {
        super(sourceId);
    }

    /*
      匹配现代汉字                     [\u4e00-\u9fa5]
      匹配更广泛的汉字                  [\u4e00-\u9fff]
      匹配中文标点                     ·【】「」、；’，。！￥…（）—：“”《》？
      匹配 ASCII 字符（字母、数字、符号） [\x00-\x7F]
      匹配 ASCII 控制字符              [\x00-\x1F\x7F]
      匹配乱码字符。即除了汉字、中文符号、字母、英文符号、数字之外的，但包括 ASCII 控制字符
      会误伤特殊符号，比如颜文字，不便排除，暂时弃用
     */
    // private final String charPattern = "[^\\u4e00-\\u9fff·【】「」、；’，。！￥…（）—：“”《》？\\x00-\\x7F]|[\\x00-\\x1F\\x7F]";

    /**
     * 正文内容过滤
     */
    public String filter(String content) {
        return filterAds(filterCharacters(content));
    }

    /**
     * 过滤字符
     */
    private String filterCharacters(String content) {
        // 替换非法的 &..;（HTML字符实体引用），可能会导致ibooks章节报错
        return content.replaceAll("&.+?;", "");
    }

    /**
     * 过滤广告
     */
    private String filterAds(String content) {
        String filteredContent = content.replaceAll(adsPattern, "");
        return HtmlUtil.removeHtmlTag(filteredContent, "div", "p", "script");
    }

}
