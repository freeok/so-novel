package com.pcdd.sonovel.util;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.Console;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONException;
import cn.hutool.json.JSONUtil;
import com.pcdd.sonovel.core.Source;
import com.pcdd.sonovel.model.AppConfig;
import com.pcdd.sonovel.model.Rule;
import lombok.experimental.UtilityClass;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

/**
 * @author pcdd
 * Created at 2025/1/29
 */
@UtilityClass
public class SourceUtils {

    private final String RULES_DIR_DEV = "bundle/rules/";
    private final String RULES_DIR_PROD = "rules/";
    private String flag = getRuleFileName();
    private List<Rule> cachedRules;

    private String getRuleFileName() {
        return ConfigWatcher.getConfig().getActiveRules();
    }

    /**
     * 获取规则文件路径
     */
    private String getRuleFilePath() {
        Path path = Paths.get(getRuleFileName());
        if (path.isAbsolute()) {
            return path.toString();
        }
        return (EnvUtils.isDev() ? RULES_DIR_DEV : RULES_DIR_PROD) + getRuleFileName();
    }

    /**
     * 根据 sourceId 获取指定规则对象
     */
    public Rule getRule(int sourceId) {
        Rule rule = getAllRules().stream()
                .filter(r -> r.getId() == sourceId)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(StrUtil.format("{} 找不到 ID 为 {} 的规则！", getRuleFileName(), sourceId)));

        return applyDefaultRule(rule);
    }

    /**
     * 从文件中读取所有规则，并进行缓存。
     * 如果缓存中已经有数据，则直接返回缓存。
     */
    public List<Rule> getAllRules() {
        File file = new File(getRuleFilePath());
        Assert.isTrue(file.exists(), "规则文件不存在：{}", file.getAbsolutePath());

        // 若缓存存在，则直接返回
        if (Objects.equals(flag, getRuleFileName()) && cachedRules != null) {
            return cachedRules;
        } else { // 激活规则变更，重置缓存
            flag = getRuleFileName();
        }

        try {
            List<Rule> rules = JSONUtil.readJSONArray(file, CharsetUtil.CHARSET_UTF_8).toList(Rule.class);
            // 填充自增 ID
            IntStream.range(0, rules.size()).forEach(i -> rules.get(i).setId(i + 1));
            // 缓存读取到的规则列表
            cachedRules = rules;
            return rules;
        } catch (JSONException e) {
            Console.error("解析规则文件失败：{}，错误信息：{}", file.getAbsolutePath(), e.getMessage());
            return Collections.emptyList();
        } catch (Exception e) {
            Console.error("读取规则文件时发生未知错误：{}，错误信息：{}", file.getAbsolutePath(), e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * 根据 sourceId 获取指定规则的 JSON 字符串
     */
    public String getRuleStr(int sourceId) {
        return JSONUtil.toJsonStr(getRule(sourceId));
    }

    /**
     * 获取可聚合搜索的书源列表
     * 排除不支持搜索的、搜索有限流的、搜索意义不大的、暂时无法访问的书源
     */
    public List<Source> getSearchableSources() {
        return getAllRules().stream()
                .filter(r -> !r.isDisabled() && r.getSearch() != null && !r.getSearch().isDisabled())
                .map(r -> {
                    AppConfig config = ConfigWatcher.getConfig();
                    config.setSourceId(r.getId());
                    return new Source(config);
                })
                .toList();
    }

    private Rule applyDefaultRule(Rule rule) {
        Rule.Search ruleSearch = rule.getSearch();
        Rule.Book ruleBook = rule.getBook();
        Rule.Toc ruleToc = rule.getToc();
        Rule.Chapter ruleChapter = rule.getChapter();

        // language
        if (StrUtil.isEmpty(rule.getLanguage())) {
            rule.setLanguage(LangUtil.getCurrentLang());
        }

        // baseUri
        if (ruleSearch != null && StrUtil.isEmpty(ruleSearch.getBaseUri())) {
            ruleSearch.setBaseUri(rule.getUrl());
        }
        if (ruleBook != null && StrUtil.isEmpty(ruleBook.getBaseUri())) {
            ruleBook.setBaseUri(rule.getUrl());
        }
        if (ruleToc != null && StrUtil.isEmpty(ruleToc.getBaseUri())) {
            ruleToc.setBaseUri(rule.getUrl());
        }
        if (ruleChapter != null && StrUtil.isEmpty(ruleChapter.getBaseUri())) {
            ruleChapter.setBaseUri(rule.getUrl());
        }

        // timeout
        if (ruleSearch != null && ruleSearch.getTimeout() == null) {
            ruleSearch.setTimeout(15);
        }
        if (ruleBook != null && ruleBook.getTimeout() == null) {
            ruleBook.setTimeout(15);
        }
        if (ruleToc != null && ruleToc.getTimeout() == null) {
            ruleToc.setTimeout(30);
        }
        if (ruleChapter != null && ruleChapter.getTimeout() == null) {
            ruleChapter.setTimeout(15);
        }

        return rule;
    }

    /**
     * 根据书籍详情页 url 匹配当前激活的书源规则
     * TODO 改为从 rules 下的全部书源获取
     */
    public Rule getSource(String bookUrl) {
        return getAllRules().stream()
                .filter(r -> bookUrl.startsWith(r.getUrl()))
                .findFirst()
                .orElse(null);
    }

}