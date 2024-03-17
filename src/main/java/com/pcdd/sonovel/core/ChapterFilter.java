package com.pcdd.sonovel.core;

import cn.hutool.http.HtmlUtil;
import lombok.experimental.UtilityClass;

import java.util.List;

/**
 * @author pcdd
 */
@UtilityClass
public class ChapterFilter {

    private final List<String> ads = List.of(
            "最新网址：www.xbiqugu.info",
            "一秒记住【文学巴士 】，精彩无弹窗免费阅读！",
            "(www.xbiquge.la 新笔趣阁)，高速全文字在线阅读！",
            "亲,点击进去,给个好评呗,分数越高更新越快,据说给香书小说打满分的最后都找到了漂亮的老婆哦!",
            "手机站全新改版升级地址：https://wap.xbiqugu.info，数据和书签与电脑站同步，无广告清新阅读！"
    );

    /**
     * 过滤词汇
     */
    public String filter(String content) {
        StringBuilder filteredContent = new StringBuilder(content);

        for (String word : ads) {
            int index = filteredContent.indexOf(word);
            while (index != -1) {
                filteredContent.delete(index, index + word.length());
                index = filteredContent.indexOf(word);
            }
        }

        // 过滤 <script> 及其内容
        return filteredContent.toString().replaceAll(HtmlUtil.RE_SCRIPT, "");
    }

    /**
     * 过滤广告，仅限书源 1，不同书源广告 html 可能不同
     */
    public String filterAds(String content) {
        return HtmlUtil.removeHtmlTag(content, "div", "p");
    }

}
