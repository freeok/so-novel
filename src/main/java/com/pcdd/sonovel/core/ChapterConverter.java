package com.pcdd.sonovel.core;

import cn.hutool.core.lang.Console;
import cn.hutool.http.HtmlUtil;
import com.pcdd.sonovel.model.NovelChapter;
import lombok.experimental.UtilityClass;

/**
 * @author pcdd
 */
@UtilityClass
public class ChapterConverter {

    public NovelChapter convert(NovelChapter novelChapter, String extName) {
        String content = filterAds(novelChapter.getContent());

        // txt 格式
        if ("txt".equals(extName)) {
            content = novelChapter.getTitle() + HtmlUtil.cleanHtmlTag(content).replace("&nbsp;", " ");
        }
        // epub 格式
        if ("epub".equals(extName)) {
            Console.log("功能开发中");
        }
        novelChapter.setContent(content);

        return novelChapter;
    }

    /**
     * 去除正文广告
     */
    private String filterAds(String content) {
        return content.replace("最新网址：www.xbiqugu.info", "")
                .replace("一秒记住【文学巴士 】，精彩无弹窗免费阅读！", "")
                .replace("(www.xbiquge.la 新笔趣阁)，高速全文字在线阅读！", "")
                .replace("亲,点击进去,给个好评呗,分数越高更新越快,据说给香书小说打满分的最后都找到了漂亮的老婆哦!", "")
                .replace("手机站全新改版升级地址：https://wap.xbiqugu.info，数据和书签与电脑站同步，无广告清新阅读！", "");
    }

}
