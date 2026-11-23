package com.pcdd.sonovel.web.service;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.pcdd.sonovel.utils.SourceUtils;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import java.io.File;

/**
 * 书源规则文件的直接读写服务（供可视化管理书源）。
 * 注意：此处读写的是规则文件的"原始 JSON"，不经过 SourceUtils.applyDefaultRule，
 * 避免把 baseUri/timeout/meta 选择器等默认值或常量写回文件造成污染。
 * 书源 ID 不持久化，等于规则数组下标 + 1，增删后下次加载自动重新编号。
 */
@UtilityClass
public class RuleFileService {

    /**
     * 读取激活规则文件的原始 JSON 数组
     */
    public JSONArray readRawRules() {
        File file = SourceUtils.getActiveRulesFile();
        Assert.isTrue(file.exists() && file.isFile(),
                "书源规则文件不存在或不是文件: {}", file.getAbsolutePath());
        return JSONUtil.readJSONArray(file, CharsetUtil.CHARSET_UTF_8);
    }

    /**
     * 写回激活规则文件（自动备份 .bak）并刷新内存缓存
     */
    @SneakyThrows
    public void writeRawRules(JSONArray rules) {
        File file = SourceUtils.getActiveRulesFile();
        if (file.exists()) {
            FileUtil.copyFile(file, new File(file.getAbsolutePath() + ".bak"), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }
        FileUtil.writeString(JSONUtil.toJsonPrettyStr(rules), file, CharsetUtil.CHARSET_UTF_8);
        SourceUtils.refreshCache();
    }

    /**
     * 按 ID（数组下标 + 1）获取原始规则
     */
    public JSONObject getRawRule(int id) {
        JSONArray arr = readRawRules();
        Assert.isTrue(id >= 1 && id <= arr.size(), "书源 ID 非法: {}", id);
        return arr.getJSONObject(id - 1);
    }

    /**
     * 新增书源，返回新 ID
     */
    public int addRule(JSONObject rule) {
        validate(rule);
        JSONArray arr = readRawRules();
        arr.add(rule);
        writeRawRules(arr);
        return arr.size();
    }

    /**
     * 修改书源
     */
    public void updateRule(int id, JSONObject rule) {
        validate(rule);
        JSONArray arr = readRawRules();
        Assert.isTrue(id >= 1 && id <= arr.size(), "书源 ID 非法: {}", id);
        arr.set(id - 1, rule);
        writeRawRules(arr);
    }

    /**
     * 删除书源
     */
    public void deleteRule(int id) {
        JSONArray arr = readRawRules();
        Assert.isTrue(id >= 1 && id <= arr.size(), "书源 ID 非法: {}", id);
        arr.remove(id - 1);
        writeRawRules(arr);
    }

    /**
     * 启用/禁用整个书源
     */
    public void toggleRuleDisabled(int id, boolean disabled) {
        JSONArray arr = readRawRules();
        Assert.isTrue(id >= 1 && id <= arr.size(), "书源 ID 非法: {}", id);
        arr.getJSONObject(id - 1).set("disabled", disabled);
        writeRawRules(arr);
    }

    /**
     * 启用/禁用书源的聚合搜索
     */
    public void toggleSearchDisabled(int id, boolean disabled) {
        JSONArray arr = readRawRules();
        Assert.isTrue(id >= 1 && id <= arr.size(), "书源 ID 非法: {}", id);
        JSONObject search = arr.getJSONObject(id - 1).getJSONObject("search");
        Assert.notNull(search, "该书源没有 search 规则，无法设置");
        search.set("disabled", disabled);
        writeRawRules(arr);
    }

    private void validate(JSONObject rule) {
        Assert.notNull(rule, "书源规则不能为空");
        Assert.isTrue(StrUtil.isNotBlank(rule.getStr("name")), "书源名称(name)不能为空");
        Assert.isTrue(StrUtil.isNotBlank(rule.getStr("url")), "书源地址(url)不能为空");
    }

}
