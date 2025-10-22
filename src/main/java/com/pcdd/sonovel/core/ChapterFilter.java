package com.pcdd.sonovel.core;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HtmlUtil;
import com.pcdd.sonovel.model.AppConfig;
import com.pcdd.sonovel.model.Chapter;
import com.pcdd.sonovel.util.CrawlUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author pcdd
 * Created at 2024/3/17
 */
public class ChapterFilter extends Source {

    public ChapterFilter(AppConfig config) {
        super(config);
    }

    /**
     * 过滤正文内容
     */
    public String filter(Chapter chapter) {
        return new FilterBuilder(chapter)
                .filterInvisibleChars(true)
                .filterAds(true)
                .filterEscape(true)
                .filterDuplicateTitle(true)
                .build();
    }

    /**
     * 建造者类，用于动态组合过滤步骤
     */
    public class FilterBuilder {
        private String title;
        private String content;
        private boolean applyInvisibleCharsFilter;
        private boolean applyEscapeFilter;
        private boolean applyAdsFilter;
        private boolean applyTitleFilter;

        public FilterBuilder(Chapter chapter) {
            this.title = chapter.getTitle();
            this.content = chapter.getContent();
        }

        /**
         * 是否启用不可见字符过滤
         */
        public FilterBuilder filterInvisibleChars(boolean apply) {
            this.applyInvisibleCharsFilter = apply;
            return this;
        }

        /**
         * 是否启用广告过滤
         */
        public FilterBuilder filterAds(boolean apply) {
            this.applyAdsFilter = apply;
            return this;
        }

        /**
         * 是否启用 HTML 实体字符过滤
         */
        public FilterBuilder filterEscape(boolean apply) {
            this.applyEscapeFilter = apply;
            return this;
        }

        /**
         * 是否启用标题过滤
         */
        public FilterBuilder filterDuplicateTitle(boolean apply) {
            this.applyTitleFilter = apply;
            return this;
        }

        /**
         * 构建最终过滤内容
         */
        public String build() {
            if (applyInvisibleCharsFilter) {
                this.content = CrawlUtils.cleanInvisibleChars(this.content);
            }

            if (applyAdsFilter) {
                String filteredContent = this.content.replaceAll(rule.getChapter().getFilterTxt(), "");
                this.content = HtmlUtil.removeHtmlTag(filteredContent, StrUtil.splitToArray(rule.getChapter().getFilterTag(), " "));
            }

            if (applyEscapeFilter) {
                // 替换 &..; (HTML 字符实体引用)，主要是 &nbsp;，可能会导致 ibooks 章节报错
                this.content = this.content.replaceAll("&[^;]+;", "");
            }

            // 确保在 EscapeFilter、AdsFilter 之后
            this.content = StrUtil.cleanBlank(this.content);

            if (applyTitleFilter) {
                // 删除正文开头的标题
                String cleanTitle = StrUtil.cleanBlank(this.title);
                this.content = content.replaceFirst("^(%s|%s)".formatted(
                        Pattern.quote(this.title), Pattern.quote(cleanTitle)
                ), "");

                // 解决某些阅读器目录无法解析 txt 中的章节名，例如：1.章节名
                Matcher matcher = Pattern.compile("^(\\d+)\\s*\\.\\s*(.+)$").matcher(this.title);
                if (matcher.find()) {
                    this.title = "第%s章 %s".formatted(matcher.group(1), matcher.group(2));
                }
            }

            // 删除全部空标签，例如 <p></p>
            return HtmlUtil.cleanEmptyTag(this.content);
        }
    }

}