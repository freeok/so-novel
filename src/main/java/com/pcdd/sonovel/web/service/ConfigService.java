package com.pcdd.sonovel.web.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.pcdd.sonovel.core.AppConfigLoader;
import com.pcdd.sonovel.model.AppConfig;
import lombok.experimental.UtilityClass;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 服务器配置（config.ini）可视化读写服务。
 * 不使用 hutool Setting.store()——实测它会丢弃全部注释；
 * 改为"行级编辑"：仅替换目标键所在行的值，注释、空行、其余键与排版原样保留。
 */
@UtilityClass
public class ConfigService {

    /**
     * 保存配置，changes 结构：{ group: { key: value } }，返回更新后的全局配置
     */
    public synchronized AppConfig save(Map<String, Map<String, Object>> changes) {
        File file = new File(AppConfigLoader.getConfigFilePath());
        List<String> lines = new ArrayList<>(FileUtil.readLines(file, StandardCharsets.UTF_8));

        changes.forEach((group, kv) -> {
            if (StrUtil.isBlank(group)) throw new IllegalArgumentException("配置组(group)不能为空");
            kv.forEach((key, raw) -> {
                if (StrUtil.isBlank(key)) throw new IllegalArgumentException("配置键(key)不能为空");
                String value = raw == null ? "" : raw.toString();
                applyChange(lines, group.trim(), key.trim(), value);
            });
        });

        // 备份后写回
        FileUtil.copyFile(file, new File(file.getAbsolutePath() + ".bak"), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        FileUtil.writeLines(lines, file, StandardCharsets.UTF_8);

        // 重新加载，并把新属性拷贝进全局单例（保持 APP_CONFIG 引用不变）
        AppConfig latest = AppConfigLoader.loadConfig();
        BeanUtil.copyProperties(latest, AppConfigLoader.APP_CONFIG);
        return AppConfigLoader.APP_CONFIG;
    }

    /**
     * 在指定 [group] 段内更新或追加 key = value，保留其他行与注释
     */
    private void applyChange(List<String> lines, String group, String key, String value) {
        int groupStart = findGroup(lines, group);

        // 组不存在：文件末尾新建
        if (groupStart < 0) {
            if (!lines.isEmpty() && StrUtil.isNotBlank(lines.get(lines.size() - 1))) {
                lines.add("");
            }
            lines.add("[" + group + "]");
            lines.add(key + " = " + value);
            return;
        }

        int groupEnd = nextGroupStart(lines, groupStart + 1);
        Pattern keyLine = Pattern.compile("^\\s*" + Pattern.quote(key) + "\\s*=.*", Pattern.CASE_INSENSITIVE);

        // 组内已存在该键：替换值行
        for (int i = groupStart + 1; i < groupEnd; i++) {
            if (keyLine.matcher(lines.get(i)).matches()) {
                lines.set(i, key + " = " + value);
                return;
            }
        }

        // 组内不存在：插入到本组最后一个非空行之后（避免插到空行下方）
        int insertAt = groupEnd;
        while (insertAt - 1 > groupStart && StrUtil.isBlank(lines.get(insertAt - 1))) {
            insertAt--;
        }
        lines.add(insertAt, key + " = " + value);
    }

    private int findGroup(List<String> lines, String group) {
        Pattern p = Pattern.compile("^\\s*\\[\\s*" + Pattern.quote(group) + "\\s*]\\s*$", Pattern.CASE_INSENSITIVE);
        for (int i = 0; i < lines.size(); i++) {
            if (p.matcher(lines.get(i)).matches()) {
                return i;
            }
        }
        return -1;
    }

    private int nextGroupStart(List<String> lines, int from) {
        for (int i = from; i < lines.size(); i++) {
            String t = lines.get(i).trim();
            if (t.startsWith("[") && t.endsWith("]")) {
                return i;
            }
        }
        return lines.size();
    }

}
