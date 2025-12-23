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

    private static final Pattern TITLE_NUMBER_PATTERN = Pattern.compile("^(\\d+)\\s*\\.\\s*(.+)$");
    private static final Pattern HTML_ENTITY_PATTERN = Pattern.compile("&[^;]+;");

    public ChapterFilter(AppConfig config) {
        super(config);
    }

    /**
     * 主过滤方法
     */
    public Chapter filter(Chapter chapter) {
        return new FilterBuilder(chapter)
                .filterInvisibleChars(true)
                .filterEscape(true)
                .filterAds(true)
                .filterTitle(true)
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
        private boolean applyDuplicateTitleFilter;

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
         * 是否启用 HTML 实体字符过滤
         */
        public FilterBuilder filterEscape(boolean apply) {
            this.applyEscapeFilter = apply;
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
         * 是否启用标题过滤
         */
        public FilterBuilder filterTitle(boolean apply) {
            this.applyDuplicateTitleFilter = apply;
            return this;
        }

        /**
         * 构建最终过滤内容
         */
        public Chapter build() {
            if (applyInvisibleCharsFilter) {
                this.content = CrawlUtils.cleanInvisibleChars(this.content);
            }

            if (applyEscapeFilter) {
                // 替换 &..; (HTML 字符实体引用)，主要是 &nbsp;，可能会导致 ibooks 章节报错
                this.content = HTML_ENTITY_PATTERN.matcher(this.content).replaceAll("");
            }

            if (applyAdsFilter) {
                String filteredContent = this.content.replaceAll(rule.getChapter().getFilterTxt(), "");
                this.content = HtmlUtil.removeHtmlTag(filteredContent, StrUtil.splitToArray(rule.getChapter().getFilterTag(), " "));
            }

            // 确保在在 EscapeFilter、AdsFilter 之执行
            this.content = StrUtil.cleanBlank(this.content);

            if (applyDuplicateTitleFilter) {
                // 删除正文开头的标题，Pattern.quote：将章节名当作纯文本，自动转义正则元字符 [、(、. 防止章节名意外破坏正则
                String regex = "^(\\s|<[^>]+>)*(%s|%s)".formatted(
                        Pattern.quote(this.title),
                        Pattern.quote(StrUtil.cleanBlank(this.title))
                );
                this.content = this.content.replaceFirst(regex, "$1");

                // 解决某些阅读器目录无法解析 txt 中的章节名，例如：1.章节名
                Matcher matcher = TITLE_NUMBER_PATTERN.matcher(this.title);
                if (matcher.find()) {
                    this.title = "第%s章 %s".formatted(matcher.group(1), matcher.group(2));
                }
            }

            return Chapter.builder()
                    .title(this.title)
                    // 删除全部空标签，例如 <p></p>
                    .content(HtmlUtil.cleanEmptyTag(this.content))
                    .build();
        }
    }

}