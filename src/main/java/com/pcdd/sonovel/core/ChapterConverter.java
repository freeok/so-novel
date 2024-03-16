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
        String content = ChapterFilter.filter(novelChapter.getContent());

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

}
