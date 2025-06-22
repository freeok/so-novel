package com.pcdd.sonovel.util;

import cn.hutool.cache.Cache;
import cn.hutool.cache.CacheUtil;
import cn.hutool.core.lang.Console;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONException;
import cn.hutool.json.JSONUtil;
import com.pcdd.sonovel.core.Source;
import com.pcdd.sonovel.model.AppConfig;
import com.pcdd.sonovel.model.Rule;
import lombok.experimental.UtilityClass;

import java.io.File;
import java.util.Collections;
import java.util.List;

/**
 * @author pcdd
 * Created at 2025/1/29
 */
@UtilityClass
public class SourceUtils {

    private static final String RULES_DIR_DEV = "bundle/rules/";
    private static final String RULES_DIR_PROD = "rules/";
    private static final String RULE_FILE_NAME = "official.json";

    private final Cache<String, List<Rule>> cache_rules;

    static {
        cache_rules = CacheUtil.newFIFOCache(1);
    }

    /**
     * 获取规则文件路径
     */
    private static String getRuleFilePath() {
        return (EnvUtils.isDev() ? RULES_DIR_DEV : RULES_DIR_PROD) + RULE_FILE_NAME;
    }

    /**
     * 根据 sourceId 获取指定规则对象
     */
    public Rule getRule(int sourceId) {
        List<Rule> allRules = getAllRules();
        if (sourceId <= 0 || sourceId > allRules.size()) {
            Console.error("无效的 sourceId：{}，规则列表大小：{}", sourceId, allRules.size());
            return null;
        }
        return applyDefaultRule(allRules.get(sourceId - 1));
    }

    /**
     * 从文件中读取所有规则，并进行缓存。
     * 如果缓存中已经有数据，则直接返回缓存。
     */
    public List<Rule> getAllRules() {
        // 如果缓存中存在，则直接返回
        if (cache_rules.get("rules") != null) {
            return cache_rules.get("rules");
        }

        File file = new File(getRuleFilePath());
        if (!file.exists()) {
            Console.error("规则文件不存在：{}", file.getAbsolutePath());
            return Collections.emptyList();
        }

        try {
            JSONArray arr = JSONUtil.readJSONArray(file, CharsetUtil.CHARSET_UTF_8);
            List<Rule> rules = arr.toList(Rule.class);
            // 缓存读取到的规则列表
            cache_rules.put("rules", rules);
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
        List<Rule> allRules = getAllRules();
        if (sourceId <= 0 || sourceId > allRules.size()) {
            Console.error("无效的 sourceId：{}，规则列表大小：{}", sourceId, allRules.size());
            return StrUtil.EMPTY_JSON;
        }
        // 直接从缓存的 List<Rule> 中获取，然后转回 JSON 字符串
        return JSONUtil.toJsonStr(allRules.get(sourceId - 1));
    }

    /**
     * 获取书源总数
     */
    public int getCount() {
        return getAllRules().size();
    }

    /**
     * 获取可聚合搜索的书源列表
     * 排除不支持搜索的、搜索有限流的、搜索意义不大的、暂时无法访问的书源
     */
    public List<Source> getSearchableSources() {
        return getAllRules().stream()
                .filter(r -> r.getSearch() != null && !r.getSearch().isDisabled())
                .map(r -> {
                    AppConfig config = ConfigUtils.config();
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
            ruleChapter.setTimeout(10);
        }

        return rule;
    }

}