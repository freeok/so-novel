package com.pcdd.sonovel.util;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.Console;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONException;
import cn.hutool.json.JSONUtil;
import com.pcdd.sonovel.core.AppConfigLoader;
import com.pcdd.sonovel.core.OkHttpClientFactory;
import com.pcdd.sonovel.core.Source;
import com.pcdd.sonovel.model.AppConfig;
import com.pcdd.sonovel.model.Rule;
import com.pcdd.sonovel.model.SourceInfo;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.IntStream;

import static org.fusesource.jansi.AnsiRenderer.render;

/**
 * @author pcdd
 * Created at 2025/1/29
 */
@UtilityClass
public class SourceUtils {

    private final String RULES_DIR_DEV = "bundle/rules/";
    private final String RULES_DIR_PROD = "rules/";
    private static final AppConfig APP_CONFIG = AppConfigLoader.APP_CONFIG;
    private List<Rule> cachedRules;

    /**
     * 获取规则文件路径
     */
    private String getRuleFilePath() {
        Path path = Paths.get(APP_CONFIG.getActiveRules());
        if (path.isAbsolute()) {
            return path.toString();
        }
        return (EnvUtils.isDev() ? RULES_DIR_DEV : RULES_DIR_PROD) + APP_CONFIG.getActiveRules();
    }

    /**
     * 根据 sourceId 获取指定规则对象
     */
    public Rule getRule(int sourceId) {
        Rule rule = getAllRules().stream()
                .filter(r -> r.getId() == sourceId)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(StrUtil.format("{} 找不到 ID 为 {} 的规则！", APP_CONFIG.getActiveRules(), sourceId)));

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
        if (cachedRules != null) {
            return cachedRules;
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
                    // 此处切勿改为 AppConfigLoader.APP_CONFIG
                    AppConfig cfg = AppConfigLoader.loadConfig();
                    cfg.setSourceId(r.getId());
                    return new Source(cfg);
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

    /**
     * 获取书源列表
     *
     * @param isDelay 是否测试连通性
     */
    @SneakyThrows
    public List<SourceInfo> getBookSources(boolean isDelay) {
        List<Rule> rules = SourceUtils.getAllRules();
        List<SourceInfo> res = new ArrayList<>();

        if (!isDelay) {
            return BeanUtil.copyToList(rules, SourceInfo.class);
        }

        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        CompletionService<SourceInfo> completionService = new ExecutorCompletionService<>(executor);
        OkHttpClient client = OkHttpClientFactory.create(AppConfigLoader.APP_CONFIG);

        for (Rule r : rules) {
            completionService.submit(() -> {
                SourceInfo source = SourceInfo.builder()
                        .id(r.getId())
                        .name(r.getName())
                        .url(r.getUrl())
                        .build();
                try {
                    Call call = client.newCall(new Request.Builder()
                            .url(r.getUrl())
                            .header("User-Agent", RandomUA.generate())
                            .head() // 只发 HEAD 请求，不获取 body，更快！
                            .build());
                    call.timeout().timeout(5, TimeUnit.SECONDS);

                    // 放这里才最准确
                    long startTime = System.currentTimeMillis();
                    try (Response resp = call.execute()) {
                        source.setDelay((int) (System.currentTimeMillis() - startTime));
                        source.setCode(resp.code());
                    }
                } catch (Exception e) {
                    source.setDelay(-1);
                    source.setCode(-1);
                    if (EnvUtils.isDev()) {
                        Console.error(render("书源 {} ({}) 测试延迟异常：{}", "red"), r.getId(), r.getName(), e.getMessage());
                    }
                }

                return source;
            });
        }

        for (int i = 0; i < rules.size(); i++) {
            // 获取最先完成的任务的结果
            res.add(completionService.take().get());
        }

        executor.shutdown();

        res.sort((o1, o2) -> {
            int delay1 = o1.getDelay() < 0 ? Integer.MAX_VALUE : o1.getDelay();
            int delay2 = o2.getDelay() < 0 ? Integer.MAX_VALUE : o2.getDelay();
            return Integer.compare(delay1, delay2);
        });

        return res;
    }

}