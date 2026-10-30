package com.pcdd.sonovel.model;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.experimental.UtilityClass;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * AI 服务的内置预设：把「不想手填 OpenAI 兼容端点」的场景收敛成几个名字。
 * <p>
 * 设计约束：
 * <ul>
 *   <li><b>预设只补空白</b>——用户显式填过的字段一律优先，预设失效也不会把已配好的服务弄丢；</li>
 *   <li><b>密钥不落盘</b>——EAC 这类本地网关的密钥由插件管理，本程序只「读取」，
 *       读不到就退回常规 api-key / 环境变量 / 启动参数，绝不因为预设而报错；</li>
 *   <li><b>地址动态解析</b>——本地网关端口可能变，host/port/key 都从插件自己的
 *       配置文件里读，而不是写死端口。</li>
 * </ul>
 *
 * @author pcdd
 */
@UtilityClass
public class AiPresets {

    /**
     * EAC router 千问国内版（qodercn 通道）
     */
    public static final String EAC_QWEN_CN = "eac-qwen-cn";

    /**
     * EAC router 千问国际版（qoder 通道）
     * <p>
     * 该通道依赖 EAC 侧的国际账号是否在线：实测出现过
     * 503 no healthy upstream，即当前无健康上游，属账号侧状态而非配置错误。
     */
    public static final String EAC_QWEN_INTL = "eac-qwen-intl";

    /**
     * 默认的 EAC 插件配置文件（相对用户目录）
     */
    private static final String DEFAULT_EAC_KEY_FILE = "~/.dsh/our-free-model/settings.json";

    /**
     * 一个预设：provider 名 -> 补什么
     */
    public record Preset(String provider, String baseUrl, String model) {
    }

    private static final Map<String, Preset> PRESETS = new LinkedHashMap<>();

    static {
        // qfmodel 而不是 qmodel：实测 qmodel 不遵守 enable_thinking:false，
        // 推理 token 会吃掉整个 max_tokens，正文只剩 "```" 这种残片；
        // qfmodel 尊重该参数，同样请求从 1368 token 降到 284 token。
        PRESETS.put(EAC_QWEN_CN, new Preset(EAC_QWEN_CN, "http://127.0.0.1:18326/v1", "qodercn/qfmodel"));
        PRESETS.put(EAC_QWEN_INTL, new Preset(EAC_QWEN_INTL, "http://127.0.0.1:18326/v1", "qoder/qfmodel"));
    }

    /**
     * provider 名是否为内置预设（空值算否）
     */
    public boolean isKnown(String provider) {
        return StrUtil.isNotBlank(provider) && PRESETS.containsKey(provider);
    }

    /**
     * 取预设，未知名返回 null
     */
    public Preset get(String provider) {
        return StrUtil.isBlank(provider) ? null : PRESETS.get(provider);
    }

    /**
     * 预设列表（供前端渲染下拉框）。
     * <p>
     * 展示名不暴露账号细节，只说明「哪个渠道的哪个版本」，并标注需要本机插件在跑。
     */
    public Map<String, String> describe() {
        Map<String, String> out = new LinkedHashMap<>();
        out.put(EAC_QWEN_CN, "EAC 千问国内版（qodercn）");
        out.put(EAC_QWEN_INTL, "EAC 千问国际版（qoder）");
        return out;
    }

    /**
     * EAC 本地网关的连接信息：从插件自己的配置文件里读，避免写死端口。
     * <p>
     * 读到 {@code chanGateway.relay} 的 host / port / key 三项；任何一项缺失
     * 或文件不存在都返回 {@code null}，由调用方退回常规密钥来源。
     *
     * @param keyFile 配置文件路径，可用 {@code ~} 表示用户目录；为空则用默认值
     */
    public Relay readEacRelay(String keyFile) {
        String path = StrUtil.blankToDefault(keyFile, DEFAULT_EAC_KEY_FILE);
        File file = new File(expandUserHome(path));
        if (!FileUtil.exist(file) || !file.isFile()) return null;
        try {
            String raw = FileUtil.readString(file, java.nio.charset.StandardCharsets.UTF_8);
            if (!JSONUtil.isTypeJSON(raw)) return null;
            JSONObject root = JSONUtil.parseObj(raw);
            JSONObject relay = root.getJSONObject("chanGateway");
            relay = relay == null ? null : relay.getJSONObject("relay");
            if (relay == null) return null;
            String host = relay.getStr("host");
            Integer port = relay.getInt("port");
            String key = relay.getStr("key");
            if (StrUtil.isBlank(host) || port == null || port <= 0 || StrUtil.isBlank(key)) return null;
            return new Relay(host, port, key);
        } catch (Exception e) {
            // 配置文件格式问题不该让整个校对启动失败，退化为常规密钥来源
            return null;
        }
    }

    /**
     * 本地网关连接信息
     */
    public record Relay(String host, int port, String key) {

        /**
         * 拼出 OpenAI 兼容的 base url
         */
        public String baseUrl() {
            return "http://" + host + ":" + port + "/v1";
        }
    }

    /**
     * 展开 {@code ~} 为用户目录
     * <p>
     * 必须用字面量替换而非 {@code replaceFirst}：{@code replaceFirst} 的替换串
     * 会解析反斜杠为转义符，{@code C:\Users\A} 会变成 {@code C:\UsersA}，
     * 路径直接失效（Windows 上的真实坑，实测踩到）。
     * 包内可见以便单测直接覆盖。
     */
    public String expandUserHome(String path) {
        if (path.startsWith("~/") || path.equals("~")) {
            String home = System.getProperty("user.home");
            if (StrUtil.isBlank(home)) return path;
            return home + path.substring(1);
        }
        return path;
    }

}
