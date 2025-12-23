package com.pcdd.sonovel.util;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.Console;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.StrUtil;
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
import java.util.ArrayList;
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
    private List<Rule> cachedAllRules;
    private List<Rule> cachedCurrentRules;

    /**
     * 根据 sourceId 获取指定规则对象
     */
    public Rule getRule(int sourceId) {
        Rule rule = getCurrentRules().stream()
                .filter(r -> r.getId() == sourceId)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(StrUtil.format("{} 找不到 ID 为 {} 的规则！", APP_CONFIG.getActiveRules(), sourceId)));
        Rule.Search ruleSearch = rule.getSearch();
        Rule.Book ruleBook = rule.getBook();
        Rule.Toc ruleToc = rule.getToc();
        Rule.Chapter ruleChapter = rule.getChapter();

        // language
        if (StrUtil.isEmpty(rule.getLanguage())) rule.setLanguage(LangUtil.getCurrentLang());
        // baseUri
        if (ruleSearch != null && StrUtil.isEmpty(ruleSearch.getBaseUri())) ruleSearch.setBaseUri(rule.getUrl());
        if (ruleBook != null && StrUtil.isEmpty(ruleBook.getBaseUri())) ruleBook.setBaseUri(rule.getUrl());
        if (ruleToc != null && StrUtil.isEmpty(ruleToc.getBaseUri())) ruleToc.setBaseUri(rule.getUrl());
        if (ruleChapter != null && StrUtil.isEmpty(ruleChapter.getBaseUri())) ruleChapter.setBaseUri(rule.getUrl());
        // timeout
        if (ruleSearch != null && ruleSearch.getTimeout() == null) ruleSearch.setTimeout(15);
        if (ruleBook != null && ruleBook.getTimeout() == null) ruleBook.setTimeout(15);
        if (ruleToc != null && ruleToc.getTimeout() == null) ruleToc.setTimeout(30);
        if (ruleChapter != null && ruleChapter.getTimeout() == null) ruleChapter.setTimeout(15);

        return rule;
    }

    /**
     * 获取当前激活的规则（带缓存）
     */
    public List<Rule> getCurrentRules() {
        if (cachedCurrentRules != null) {
            return cachedCurrentRules;
        }
        cachedCurrentRules = loadCurrentRules();
        return cachedCurrentRules;
    }

    /**
     * 获取全部规则（带缓存）
     */
    public List<Rule> getAllRules() {
        if (cachedAllRules != null) {
            return cachedAllRules;
        }
        cachedAllRules = loadAllRules();
        return cachedAllRules;
    }

    private List<Rule> loadCurrentRules() {
        String baseDir = EnvUtils.isDev() ? RULES_DIR_DEV : RULES_DIR_PROD;
        String pathname = baseDir + APP_CONFIG.getActiveRules();
        return loadRulesFromPath(pathname);
    }

    private List<Rule> loadAllRules() {
        String baseDir = EnvUtils.isDev() ? RULES_DIR_DEV : RULES_DIR_PROD;
        return loadRulesFromPath(baseDir);
    }

    private List<Rule> loadRulesFromPath(String pathname) {
        List<File> files = FileUtil.loopFiles(new File(pathname),
                f -> f.getName().endsWith(".json"));

        Assert.notEmpty(files, "规则文件不存在");

        List<Rule> rules = new ArrayList<>();
        for (File file : files) {
            rules.addAll(
                    JSONUtil.readJSONArray(file, CharsetUtil.CHARSET_UTF_8)
                            .toList(Rule.class)
            );
        }

        // 填充自增 ID
        IntStream.range(0, rules.size())
                .forEach(i -> rules.get(i).setId(i + 1));

        return rules;
    }

    /**
     * 获取可聚合搜索的书源列表。排除不支持搜索的、搜索有限流的、搜索意义不大的、暂时无法访问的书源
     */
    public List<Source> getSearchableSources() {
        return getCurrentRules().stream()
                .filter(r -> !r.isDisabled() && r.getSearch() != null && !r.getSearch().isDisabled())
                .map(r -> {
                    // 此处切勿改为 AppConfigLoader.APP_CONFIG
                    AppConfig cfg = AppConfigLoader.loadConfig();
                    cfg.setSourceId(r.getId());
                    return new Source(cfg);
                })
                .toList();
    }

    /**
     * 根据书籍详情页 url 匹配书源规则
     */
    public Rule getSource(String bookUrl) {
        List<Rule> allRules = getAllRules();
        return allRules.stream()
                .filter(r -> bookUrl.startsWith(r.getUrl()))
                .findFirst()
                .orElse(null);
    }

    public List<SourceInfo> getBookSources() {
        List<Rule> rules = SourceUtils.getCurrentRules();
        return BeanUtil.copyToList(rules, SourceInfo.class);
    }

    @SneakyThrows
    public List<SourceInfo> getBookSourcesWithAvailabilityCheck() {
        List<Rule> rules = SourceUtils.getCurrentRules();
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
                    call.timeout().timeout(3, TimeUnit.SECONDS);

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
                        Console.error(render("书源 {} ({}) 测试连通性异常：{}", "red"), r.getId(), r.getName(), e.getMessage());
                    }
                }

                return source;
            });
        }

        List<SourceInfo> res = new ArrayList<>();
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