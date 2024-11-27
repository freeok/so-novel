package com.pcdd.sonovel.core;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HtmlUtil;

/**
 * @author pcdd
 */
public class ChapterFilter extends Source {

    public ChapterFilter(int sourceId) {
        super(sourceId);
    }

    /**
     * 使用建造者进行过滤
     */
    public String filter(String content) {
        return new FilterBuilder(content)
                .filterCharacters(true)
                .filterAds(true)
                .build();
    }

    /**
     * 建造者类，用于动态组合过滤步骤
     */
    public class FilterBuilder {
        private String content;
        private boolean applyCharacterFilter;
        private boolean applyAdFilter;

        public FilterBuilder(String content) {
            this.content = content;
        }

        /**
         * 是否应用字符过滤
         */
        public FilterBuilder filterCharacters(boolean apply) {
            this.applyCharacterFilter = apply;
            return this;
        }

        /**
         * 是否应用广告过滤
         */
        public FilterBuilder filterAds(boolean apply) {
            this.applyAdFilter = apply;
            return this;
        }

        /**
         * 构建最终过滤内容
         */
        public String build() {
            if (applyCharacterFilter) {
                // 替换非法的 &..;（HTML字符实体引用），可能会导致ibooks章节报错
                this.content = this.content.replaceAll("&[^;]+;", "");
            }
            if (applyAdFilter) {
                String filteredContent = this.content.replaceAll(rule.getChapter().getFilterTxt(), "");
                this.content = HtmlUtil.removeHtmlTag(filteredContent, StrUtil.splitToArray(rule.getChapter().getFilterTag(), " "));
            }
            return this.content;
        }
    }

}
