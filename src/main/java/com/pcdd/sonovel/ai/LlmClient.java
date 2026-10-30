package com.pcdd.sonovel.ai;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.pcdd.sonovel.model.AiConfig;
import com.pcdd.sonovel.utils.CrawlUtils;
import lombok.AllArgsConstructor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * OpenAI Chat Completions 兼容客户端（DeepSeek / 通义 / Kimi / 智谱 / OpenAI / Ollama /
 * vLLM / LM Studio 等均兼容，只需改 base-url 与 model）。
 * <p>
 * 只负责「发一次请求、拿到 JSON」，不做任何业务校验。协议要点：
 * <ul>
 *   <li>用 {@code /chat/completions}，system 提示固定校对规则，user 传背景资料 + 待校对文本；</li>
 *   <li>temperature 取 0（校对是确定性任务，不要发挥）；</li>
 *   <li>兼容部分服务商不返回 {@code response_format} 的情况：解析时抽取首个 JSON 数组。</li>
 * </ul>
 *
 * @author pcdd
 */
@AllArgsConstructor
public class LlmClient implements ProofreadEngine.Suggester {

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private final AiConfig cfg;
    private final OkHttpClient client;

    private static OkHttpClient sharedClient(AiConfig cfg) {
        return new OkHttpClient.Builder()
                .connectTimeout(cfg.getTimeout(), TimeUnit.SECONDS)
                .readTimeout(cfg.getTimeout(), TimeUnit.SECONDS)
                .writeTimeout(cfg.getTimeout(), TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build();
    }

    /**
     * 一条建议的替换
     */
    public record Replacement(String find, String replace, String reason) {
    }

    /**
     * 请求模型给出文本的错别字替换建议。
     *
     * @param background 背景资料块（百科摘要 + 专有名词白名单）
     * @param text       待校对纯文本
     */
    public List<Replacement> suggest(String background, String text) throws Exception {
        JSONObject body = new JSONObject();
        body.set("model", cfg.getModel());
        body.set("temperature", 0);
        body.set("max_tokens", cfg.getMaxTokens());
        // 明确不需要流式，避免个别实现默认 SSE
        body.set("stream", false);

        /**
         * 思考开关只发给「有内置预设的服务」：EAC 这类网关默认开思考，
         * 而校对是确定性任务，推理链纯属浪费 —— 实测同一请求开思考时
         * reasoning tokens 占 completion 的 83%~92%（763 里有 635），
         * 关掉后 token 降 3.8 倍、耗时降 2~3 倍。
         * <p>
         * 自定义 OpenAI 兼容服务不保证接受这个字段，客户端不该因为一个
         * 可选参数而被拒，所以它们保持不发送。
         */
        if (cfg.isProviderBased()) {
            body.set("enable_thinking", Integer.valueOf(1).equals(cfg.getEnableThinking()));
        }

        JSONArray messages = new JSONArray();
        messages.add(msg("system", SYSTEM_PROMPT));
        messages.add(msg("user", USER_TEMPLATE
                .replace("{background}", background == null ? "（无）" : background)
                .replace("{text}", text)));
        body.set("messages", messages);
        body.set("response_format", new JSONObject().set("type", "json_object"));

        String url = StrUtil.removeSuffix(cfg.getBaseUrl(), "/") + "/chat/completions";
        Request.Builder builder = new Request.Builder()
                .url(url)
                .header("Authorization", "Bearer " + cfg.getApiKey())
                .header("Content-Type", "application/json")
                .post(RequestBody.create(JSON, body.toString()));

        try (Response resp = CrawlUtils.request(client, builder, cfg.getTimeout())) {
            if (!resp.isSuccessful()) {
                String err = StrUtil.maxLength(resp.body().string(), 400);
                throw new IllegalStateException("AI 服务返回 HTTP " + resp.code() + "：" + err);
            }
            String raw = resp.body().string();
            return parse(raw);
        }
    }

    private JSONObject msg(String role, String content) {
        return new JSONObject().set("role", role).set("content", content);
    }

    private static final String SYSTEM_PROMPT = """
            你是中文小说的校对工具，只做一件事：找出「确定是错别字或录入错误」的字词，给出替换建议。

            必须遵守：
            1. 只改错别字（同音/形近误写）、明显的重复字（如「他说说」）、明显的多字漏字。
            2. 绝不改动：人名、地名、宗门/组织名、功法/技能/物品名、境界与术语、方言口语、
               角色口癖、故意的谐音梗或双关、作者的文风用词。背景资料中列出的词一律不许动。
            3. 拿不准就不要提。你的建议会被逐条应用，一条错误建议就会毁掉原文一处内容。
            4. 不要润色、不要改写句式、不要统一标点、不要增删空行、不要把简体转繁体（或反向）。
            5. find 必须是给定文本中逐字出现的原文片段（含足够上下文以便唯一定位），
               长度不超过 20 字；replace 与 find 长度差不超过 2 字，且必须只改正错字部分。
            6. 网文正文中常见「的/地/得」混用，除非明显影响理解，否则不要改。

            只输出 JSON，不要任何解释或代码块标记，格式：
            {"replacements":[{"find":"原文片段","replace":"改正后片段","reason":"简短理由"}]}
            若没有确定的错误，输出 {"replacements":[]}
            """;

    private static final String USER_TEMPLATE = """
            【背景资料】
            {background}

            【待校对文本】
            {text}
            """;

    /**
     * 解析模型输出：容忍 ```json 包裹、前后多余文字、只返回数组等非标情况
     */
    private List<Replacement> parse(String raw) {
        List<Replacement> list = new ArrayList<>();
        String content = extractContent(raw);
        if (StrUtil.isBlank(content)) return list;

        String jsonStr = firstJsonObject(content);
        if (jsonStr == null) return list;

        JSONObject obj = JSONUtil.parseObj(jsonStr);
        JSONArray arr = obj.getJSONArray("replacements");
        if (arr == null) {
            // 兼容模型直接返回数组
            try {
                arr = JSONUtil.parseArray(content);
            } catch (Exception ignore) {
                return list;
            }
        }
        if (arr == null) return list;

        for (Object o : arr) {
            if (!(o instanceof JSONObject item)) continue;
            String find = item.getStr("find");
            String replace = item.getStr("replace");
            if (StrUtil.isBlank(find) || replace == null || find.equals(replace)) continue;
            list.add(new Replacement(find, replace, StrUtil.blankToDefault(item.getStr("reason"), "")));
        }
        return list;
    }

    /**
     * OpenAI 标准响应：choices[0].message.content；兼容直接返回文本的服务
     */
    private String extractContent(String raw) {
        if (StrUtil.isBlank(raw) || !JSONUtil.isTypeJSON(raw)) return raw;
        try {
            JSONObject json = JSONUtil.parseObj(raw);
            JSONArray choices = json.getJSONArray("choices");
            if (choices != null && !choices.isEmpty()) {
                JSONObject first = choices.getJSONObject(0);
                JSONObject message = first.getJSONObject("message");
                if (message != null) {
                    String c = message.getStr("content");
                    if (StrUtil.isNotBlank(c)) return c;
                }
                // o1/推理系列可能只有 text
                if (first.getStr("text") != null) return first.getStr("text");
            }
            // 部分兼容服务直接返回 {content: "..."} 或 {output: ...}
            String c = json.getStr("content");
            if (StrUtil.isNotBlank(c)) return c;
        } catch (Exception ignore) {
        }
        return raw;
    }

    /**
     * 抽取第一个花括号配对的 JSON（处理 markdown 代码块与模型废话）
     */
    private String firstJsonObject(String s) {
        int start = s.indexOf('{');
        if (start < 0) return null;
        int depth = 0;
        boolean inStr = false;
        boolean esc = false;
        for (int i = start; i < s.length(); i++) {
            char c = s.charAt(i);
            if (inStr) {
                if (esc) {
                    esc = false;
                } else if (c == '\\') {
                    esc = true;
                } else if (c == '"') {
                    inStr = false;
                }
                continue;
            }
            if (c == '"') inStr = true;
            else if (c == '{') depth++;
            else if (c == '}') {
                depth--;
                if (depth == 0) return s.substring(start, i + 1);
            }
        }
        return null;
    }

    public static LlmClient of(AiConfig cfg) {
        return new LlmClient(cfg, sharedClient(cfg));
    }

}
