package com.pcdd.sonovel.ai;

import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.pcdd.sonovel.context.HttpClientContext;
import com.pcdd.sonovel.utils.CrawlUtils;
import com.pcdd.sonovel.utils.LogUtils;
import lombok.experimental.UtilityClass;
import okhttp3.OkHttpClient;
import okhttp3.Response;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 百科背景调研客户端：在 AI 校对之前，先搞清楚「这本书在讲什么」。
 * <p>
 * 为什么必须先调研：大模型改错别字时，会把「功法名/人名/地名/术语」当成错别字改掉
 * （例如把「克莱恩」改成「克菜恩」、把「序列」当成「序烈」）。只有把权威百科的摘要与
 * 条目内链作为白名单喂给模型，才能把改动限制在「真正的错字」上。
 * <p>
 * 数据源选用无需 API key、无需 cookie 的公开 MediaWiki 接口，并按可达性依次降级：
 * <ol>
 *   <li>中文维基百科（覆盖知名网文，如《诡秘之主》《斗破苍穹》）</li>
 *   <li>萌娘百科（ACG/网文覆盖更广，含角色与设定词条）</li>
 *   <li>都失败：仅用书源自带简介 + 本地文本统计（此时策略应格外保守）</li>
 * </ol>
 * 注意：国内网络下维基系需 HTTP 代理，走 config.ini 的 [proxy] 配置（由主 HttpClient 承担）。
 * 本类所有方法均为「尽力而为」，任何异常都不外抛，保证不影响正常下载流程。
 *
 * @author pcdd
 */
@UtilityClass
public class WikiResearcher {

    private final String WIKI_API = "https://zh.wikipedia.org/w/api.php";
    private final String MOE_API = "https://zh.moegirl.org.cn/api.php";

    /**
     * 摘要最大字符数（MediaWiki extracts 的 exchars 上限为 1200）
     */
    private final int EXTRACT_CHARS = 1200;
    private final int LINK_LIMIT = 60;

    /**
     * 一次调研的完整产出
     */
    public static class Research {
        private String summary;
        private String sourceNote;
        private final Set<String> names = new LinkedHashSet<>();
        private boolean hit;

        public String getSummary() {
            return summary;
        }

        public String getSourceNote() {
            return sourceNote;
        }

        public Set<String> getNames() {
            return names;
        }

        public boolean isHit() {
            return hit;
        }
    }

    /**
     * 按书名调研（先用 opensearch 定位真实条目，兼容带消歧后缀的标题）。
     *
     * @param bookName 书名
     * @param author   作者（用于在无同名条目时二次定位）
     */
    public Research research(String bookName, String author) {
        Research res = new Research();
        if (StrUtil.isBlank(bookName)) return res;

        for (Endpoint ep : List.of(new Endpoint(WIKI_API, "维基百科"), new Endpoint(MOE_API, "萌娘百科"))) {
            try {
                String title = locate(ep, bookName, author);
                if (title == null) continue;

                String extract = fetchExtract(ep, title);
                if (StrUtil.isBlank(extract)) continue;

                res.summary = extract;
                res.sourceNote = ep.label + "《" + title + "》";
                res.hit = true;
                // 条目内链就是本书的专有名词（人名/地名/组织/术语），白名单价值最高
                res.names.addAll(fetchLinks(ep, title));
                LogUtils.info("AI 校对背景调研命中：{}（{}）共 {} 个专有名词候选",
                        title, ep.label, res.names.size());
                return res;
            } catch (Exception e) {
                LogUtils.info("百科调研跳过 {}：{}", ep.label, e.getMessage());
            }
        }

        if (!res.hit) {
            LogUtils.info("未在公开百科检索到《{}》，AI 校对将只依据书源简介与正文上下文（保守模式）", bookName);
        }
        return res;
    }

    private record Endpoint(String api, String label) {
    }

    /**
     * 定位条目标题：优先精确同名，其次 opensearch 首条，最后「书名 (作者)」消歧形式
     */
    private String locate(Endpoint ep, String bookName, String author) throws Exception {
        // 1) 直接当作标题试探（多数作品条目名即书名）
        if (StrUtil.isNotBlank(pageTitle(ep, bookName))) {
            return bookName;
        }
        // 2) 前缀搜索
        List<String> candidates = opensearch(ep, bookName);
        for (String c : candidates) {
            if (c.equals(bookName)) return c;
        }
        // 3) 带消歧后缀，如「诡秘之主 (小说)」
        for (String c : candidates) {
            if (c.startsWith(bookName)) return c;
        }
        // 4) 有作者信息时再试一次
        if (StrUtil.isNotBlank(author)) {
            for (String c : opensearch(ep, bookName + " " + author)) {
                if (c.startsWith(bookName)) return c;
            }
            if (!candidates.isEmpty()) return candidates.get(0);
        }
        return null;
    }

    /**
     * 确认标题是否存在（返回规范化标题，不存在返回 null）
     */
    private String pageTitle(Endpoint ep, String title) throws Exception {
        String url = ep.api + "?action=query&format=json&utf8=1&redirects=1"
                + "&titles=" + URLUtil.encode(title);
        JSONObject json = get(url);
        JSONObject pages = json.getJSONObject("query.pages");
        if (pages == null || pages.isEmpty()) return null;
        for (String key : pages.keySet()) {
            if ("-1".equals(key)) return null;
            return pages.getJSONObject(key).getStr("title");
        }
        return null;
    }

    private List<String> opensearch(Endpoint ep, String query) throws Exception {
        String url = ep.api + "?action=opensearch&format=json&utf8=1&limit=5&redirects=resolve"
                + "&search=" + URLUtil.encode(query);
        JSONArray arr = JSONUtil.parseArray(getRaw(url));
        if (arr.size() < 2) return List.of();
        JSONArray titles = arr.getJSONArray(1);
        List<String> list = new ArrayList<>();
        for (Object o : titles) {
            if (o != null) list.add(o.toString());
        }
        return list;
    }

    /**
     * 取纯文本摘要（含简介/世界观，够模型判断「萧炎」不是「肖炎」的错字）
     */
    private String fetchExtract(Endpoint ep, String title) throws Exception {
        String url = ep.api + "?action=query&format=json&utf8=1&redirects=1"
                + "&prop=extracts&explaintext=1&exchars=" + EXTRACT_CHARS
                + "&titles=" + URLUtil.encode(title);
        JSONObject pages = get(url).getJSONObject("query.pages");
        if (pages == null) return null;
        String best = null;
        for (String key : pages.keySet()) {
            JSONObject page = pages.getJSONObject(key);
            if (page == null || "-1".equals(key)) continue;
            String ex = page.getStr("extract");
            if (StrUtil.isNotBlank(ex) && (best == null || ex.length() > best.length())) {
                best = ex;
            }
        }
        return best;
    }

    /**
     * 取条目内链标题：维基百科/萌娘的内链几乎必然是作品相关专有名词
     */
    private Set<String> fetchLinks(Endpoint ep, String title) throws Exception {
        String url = ep.api + "?action=query&format=json&utf8=1&redirects=1"
                + "&generator=links&titles=" + URLUtil.encode(title)
                + "&gpllimit=" + LINK_LIMIT + "&gplnamespace=0";
        JSONObject pages = get(url).getJSONObject("query.pages");
        Set<String> names = new LinkedHashSet<>();
        if (pages == null) return names;
        for (String key : pages.keySet()) {
            JSONObject page = pages.getJSONObject(key);
            JSONArray links = page == null ? null : page.getJSONArray("links");
            if (links == null) continue;
            for (Object o : links) {
                String t = ((JSONObject) o).getStr("title");
                if (StrUtil.isNotBlank(t) && !t.contains(":")) {
                    names.add(t);
                }
            }
        }
        return names;
    }

    private JSONObject get(String url) throws Exception {
        return JSONUtil.parseObj(getRaw(url));
    }

    private String getRaw(String url) throws Exception {
        OkHttpClient client = HttpClientContext.get();
        if (client == null) {
            throw new IllegalStateException("HttpClient 未初始化");
        }
        try (Response resp = CrawlUtils.request(client, url, 20)) {
            if (!resp.isSuccessful()) {
                throw new IllegalStateException("HTTP " + resp.code());
            }
            return resp.body().string();
        }
    }

}
