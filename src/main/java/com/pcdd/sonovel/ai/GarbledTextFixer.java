package com.pcdd.sonovel.ai;

import cn.hutool.core.util.StrUtil;
import lombok.experimental.UtilityClass;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 确定性乱码修复器（不依赖 AI，零成本、零语义风险）。
 * <p>
 * 小说站正文乱码的三大来源：
 * <ol>
 *   <li><b>编码错配（mojibake）</b>：UTF-8 字节流被按 Latin-1/GBK 误解，或反之。可逆，能救回来。</li>
 *   <li><b>不可逆替换符</b>：解码失败产生的 U+FFFD（）以及其 GBK 二次形态「锟斤拷」，
 *       原字节已丢失，只能删除。</li>
 *   <li><b>私有区/控制字符/残留标记</b>：站点的 PUA 字体反爬字符、零宽字符、HTML 实体与标签碎片。</li>
 * </ol>
 * 该类只处理「明显不是正常中文文本」的片段，判定采用<b>适应度比较</b>：
 * 只有当修复结果的可读度（CJK 占比）显著优于原文时才替换，因此对正常文本是幂等的。
 *
 * @author pcdd
 */
@UtilityClass
public class GarbledTextFixer {

    /**
     * 不可逆乱码指纹：U+FFFD 被 GBK 反复编解码后的形态，字节已丢失，无法还原
     */
    private final Pattern IRREVERSIBLE_TOKEN = Pattern.compile("锟斤拷|\ufffd+");

    /**
     * 内存填充/调试残留类乱码
     */
    private final Pattern DEBUG_RESIDUE = Pattern.compile("(烫烫烫|屯屯屯|锟铜锟铜|ﾥﾥ)");

    /**
     * PUA（私有使用区）：字体反爬站点常用其映射假字形，正文里一律视为垃圾。
     * 注意仅在无法还原时删除，删除后可能留下空缺，故只删连续 PUA 且不做补全。
     * 增补平面码点用代理对区间匹配（Java 正则不支持 \\x{...} 形式写在字符类里的写法，代理对最稳）。
     */
    private final Pattern PUA_CHARS = Pattern.compile("[\\uE000-\\uF8FF]|[\\uDB80-\\uDBFF][\\uDC00-\\uDFFF]");

    /**
     * 残留的数字/命名 HTML 实体（ChapterFilter 会再清一遍，这里提前处理避免影响判定）
     */
    private final Pattern NUMERIC_ENTITY = Pattern.compile("&#[xX]?[0-9a-fA-F]+;");

    /**
     * 西文扩展区密集片段（典型 Latin-1 误读 UTF-8 的外观：「æµ·」「ã」）
     */
    private final Pattern LATIN_MOJIBAKE = Pattern.compile("[\\u0080-\\u00FF]{2,}[\\u0080-\\u00FF\\u0100-\\u017F\\u02C6\\u02DC\\u2013\\u2014\\u2018\\u2019\\u201C\\u201D\\u2020\\u2021\\u2022\\u2026\\u20AC\\u2122]*");

    /**
     * GBK 误读 UTF-8 的外观无法用码点区间安全表达 —— 实测「餐(9910)/骸(9ab8)/验(9a8c)」等
     * 常用字都落在所谓的「生僻区间」内，任何宽区间启发式都会把正常中文误判成乱码。
     * 因此这里只认 CJK 扩展 A 区（0x3400-0x4DBF，正常小说正文不会出现），
     * 其余情况交给 latin mojibake 路径 + readability 提升判定兜底。
     */
    private final Pattern CJK_RARE_MOJIBAKE = Pattern.compile("[\\u3400-\\u4DBF]{2,}");

    /**
     * 可尝试的「误读编码」组合：(把当前字符编回字节所用编码, 重新解码字节所用编码)
     */
    private final Map<String, Charset[]> RECOVERY_CHAINS = recoveryChains();

    private Map<String, Charset[]> recoveryChains() {
        Map<String, Charset[]> map = new LinkedHashMap<>();
        // UTF-8 字节被 Latin-1 / cp1252 误读
        map.put("latin1->utf8", new Charset[]{ISO_8859_1(), StandardCharsets.UTF_8});
        map.put("cp1252->utf8", new Charset[]{cp1252(), StandardCharsets.UTF_8});
        // UTF-8 字节被 GBK 误读
        map.put("gbk->utf8", new Charset[]{gbk(), StandardCharsets.UTF_8});
        // GBK 字节被 UTF-8 误读（先按 UTF-8 编回字节再按 GBK 解）
        map.put("utf8->gbk", new Charset[]{StandardCharsets.UTF_8, gbk()});
        // GBK 字节被 Big5 误读（繁简站点互转前的常见错位）
        map.put("big5->gbk", new Charset[]{big5(), gbk()});
        return map;
    }

    private Charset ISO_8859_1() {
        return StandardCharsets.ISO_8859_1;
    }

    private Charset cp1252() {
        return Charset.forName("windows-1252");
    }

    private Charset gbk() {
        return Charset.forName("GBK");
    }

    private Charset big5() {
        return Charset.forName("Big5");
    }

    /**
     * 修复结果，带统计信息供审计与日志
     */
    public static class Result {
        private final String text;
        private final int irreversibleRemoved;
        private final int mojibakeFixed;
        private final int noiseRemoved;

        public Result(String text, int irreversibleRemoved, int mojibakeFixed, int noiseRemoved) {
            this.text = text;
            this.irreversibleRemoved = irreversibleRemoved;
            this.mojibakeFixed = mojibakeFixed;
            this.noiseRemoved = noiseRemoved;
        }

        public String getText() {
            return text;
        }

        public int getIrreversibleRemoved() {
            return irreversibleRemoved;
        }

        public int getMojibakeFixed() {
            return mojibakeFixed;
        }

        public int getNoiseRemoved() {
            return noiseRemoved;
        }

        public boolean changed() {
            return irreversibleRemoved > 0 || mojibakeFixed > 0 || noiseRemoved > 0;
        }

        public int total() {
            return irreversibleRemoved + mojibakeFixed + noiseRemoved;
        }
    }

    /**
     * 是否需要修复（快速预判，避免对干净文本做无谓的正则与编码往返）
     */
    public boolean looksGarbled(String text) {
        if (StrUtil.isBlank(text)) return false;
        return IRREVERSIBLE_TOKEN.matcher(text).find()
                || DEBUG_RESIDUE.matcher(text).find()
                || PUA_CHARS.matcher(text).find()
                || LATIN_MOJIBAKE.matcher(text).find()
                || NUMERIC_ENTITY.matcher(text).find()
                || CJK_RARE_MOJIBAKE.matcher(text).find();
    }

    /**
     * 修复正文乱码。对正常文本幂等：适应度不提升则保持原样。
     *
     * @param text        原文（可含 HTML 标签，标签内容不受影响）
     * @param targetLangs 合法文字体系（如 zh/ja/en），决定哪些字符视为「正常」而非乱码
     */
    public Result fix(String text, String... targetLangs) {
        if (StrUtil.isBlank(text)) {
            return new Result(text, 0, 0, 0);
        }
        // 快速预判：没有任何乱码特征就不进任何改写分支。
        // 这一条保证了对正常文本的严格幂等，也省下每章一次全文正则扫描的开销。
        if (!looksGarbled(text)) {
            return new Result(text, 0, 0, 0);
        }

        String s = text;
        int irreversible = 0;
        int fixed = 0;
        int noise = 0;

        // 1) 数字实体 -> 真实字符（可能是合法内容，也可能是 &amp;#133; 之类的混淆）
        java.util.regex.Matcher em = NUMERIC_ENTITY.matcher(s);
        StringBuilder sb = new StringBuilder();
        while (em.find()) {
            String decoded = decodeEntity(em.group());
            if (decoded == null) {
                // 解不出来（多层嵌套/被截断）说明是垃圾，删
                em.appendReplacement(sb, "");
                noise++;
            } else {
                em.appendReplacement(sb, java.util.regex.Matcher.quoteReplacement(decoded));
            }
        }
        em.appendTail(sb);
        s = sb.toString();

        // 2) 不可逆乱码指纹：删除（无法还原，留着只会污染 AI 校对与阅读）
        irreversible = countTokens(IRREVERSIBLE_TOKEN, s);
        s = IRREVERSIBLE_TOKEN.matcher(s).replaceAll("");
        int debugResidue = countTokens(DEBUG_RESIDUE, s);
        s = DEBUG_RESIDUE.matcher(s).replaceAll("");
        noise += debugResidue;

        // 3) mojibake 逐段还原
        StringBuilder out = new StringBuilder(s.length());
        java.util.regex.Matcher chunk = Pattern.compile(
                "[^\\u4E00-\\u9FA5\\u3000-\\u303F\\uFF00-\\uFFEF\\n\\r\\t ]+"
        ).matcher(s);
        int last = 0;
        while (chunk.find()) {
            String seg = chunk.group();
            if (!needsRecovery(seg)) continue;
            String recovered = recover(seg, targetLangs);
            if (recovered != null) {
                out.append(s, last, chunk.start()).append(recovered);
                last = chunk.end();
                fixed++;
            }
        }
        out.append(s.substring(last));
        s = out.toString();

        // 4) PUA 字体反爬字符：确认无法还原后删除
        java.util.regex.Matcher pm = PUA_CHARS.matcher(s);
        if (pm.find()) {
            noise += countTokens(PUA_CHARS, s);
            s = pm.replaceAll("");
        }

        // 5) 收尾：仅在确实删除了乱码后折叠留下的空洞空白。
        //    正常文本的排版（含作者有意留的空格）一律不动，避免「修乱码」顺带改坏格式。
        boolean removedAnything = irreversible > 0 || noise > 0;
        if (removedAnything) {
            s = s.replaceAll("(?<!\n)[ \u3000]{3,}(?! *\n)", "  ");
        }

        return new Result(s, irreversible, fixed, noise);
    }

    /**
     * 判断一段非 CJK 文本是否像 mojibake。
     * <p>
     * 只用两类安全信号：密集西文扩展字符（Latin-1 误读特征）、CJK 扩展 A 区生僻字（GBK 误读特征）。
     * 正文基本区（4E00-9FFF）一律视为正常，避免误伤常用汉字。
     */
    private boolean needsRecovery(String seg) {
        if (seg.length() < 2) return false;
        long highLatin = seg.codePoints()
                .filter(cp -> (cp >= 0x80 && cp <= 0xFF) || (cp >= 0x100 && cp <= 0x17F))
                .count();
        if (highLatin >= 2 && highLatin * 2 >= seg.length()) return true;
        long rare = seg.codePoints().filter(cp -> cp >= 0x3400 && cp <= 0x4DBF).count();
        return rare >= 2;
    }

    /**
     * 尝试把 mojibake 片段还原。仅当可读度显著提升才返回结果。
     */
    private String recover(String seg, String... langs) {
        double base = readability(seg, langs);
        String best = null;
        double bestScore = base + 0.25; // 要求明显更好，避免正常英文/数字被误伤

        for (Map.Entry<String, Charset[]> e : RECOVERY_CHAINS.entrySet()) {
            String candidate = roundTrip(seg, e.getValue()[0], e.getValue()[1]);
            if (candidate == null || candidate.indexOf('\ufffd') >= 0) continue;
            double score = readability(candidate, langs);
            // 还原后不应仍含需要修复的字符
            if (score > bestScore && !needsRecovery(candidate)) {
                bestScore = score;
                best = candidate;
            }
        }
        return best;
    }

    /**
     * 编码往返：按 from 把字符编回字节，再按 to 解码。任一环节严格失败则返回 null。
     */
    private String roundTrip(String seg, Charset from, Charset to) {
        try {
            CharsetEncoder enc = from.newEncoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT);
            ByteBuffer bb = enc.encode(CharBuffer.wrap(seg));

            CharsetDecoder dec = to.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT);
            return dec.decode(bb).toString();
        } catch (CharacterCodingException | RuntimeException e) {
            return null;
        }
    }

    /**
     * 可读度评分：目标文字占比 - 生僻/符号噪声占比，值域约 [-1, 1]。
     */
    private double readability(String s, String... langs) {
        if (s.isEmpty()) return 0;
        boolean wantJa = false;
        for (String l : langs) {
            if (l != null && l.toLowerCase().startsWith("ja")) wantJa = true;
        }
        int total = 0;
        int good = 0;
        int bad = 0;
        for (int cp : s.codePoints().toArray()) {
            if (Character.isWhitespace(cp)) continue;
            total++;
            if (isCjk(cp) || (wantJa && isKana(cp))) {
                good++;
            } else if (isPlainLatin(cp)) {
                good++;
            } else if (isCommonPunct(cp)) {
                // 中性
            } else if (cp >= 0x80 && cp <= 0xFF) {
                bad += 2; // 西文扩展区密集 = mojibake 特征
            } else if (cp >= 0xE000 && cp <= 0xF8FF) {
                bad += 3;
            } else {
                bad++;
            }
        }
        if (total == 0) return 0;
        return (double) (good - bad) / total;
    }

    private boolean isCjk(int cp) {
        return (cp >= 0x4E00 && cp <= 0x9FFF)
                || (cp >= 0x3400 && cp <= 0x4DBF)
                || (cp >= 0xF900 && cp <= 0xFAFF);
    }

    private boolean isKana(int cp) {
        return (cp >= 0x3040 && cp <= 0x30FF);
    }

    private boolean isPlainLatin(int cp) {
        return (cp >= 0x20 && cp <= 0x7E);
    }

    private boolean isCommonPunct(int cp) {
        return (cp >= 0x3000 && cp <= 0x303F)
                || (cp >= 0xFF00 && cp <= 0xFFEF)
                || "“”‘’—…～·〈〉《》【】°℃±×÷".indexOf(cp) >= 0;
    }

    private String decodeEntity(String entity) {
        try {
            String body = entity.substring(2, entity.length() - 1);
            int code;
            if (body.startsWith("x") || body.startsWith("X")) {
                code = Integer.parseInt(body.substring(1), 16);
            } else {
                code = Integer.parseInt(body);
            }
            if (code < 0x20 || code > 0x10FFFF || Character.isSurrogate((char) code)) return null;
            return new String(Character.toChars(code));
        } catch (Exception e) {
            return null;
        }
    }

    private int countTokens(Pattern p, String s) {
        int n = 0;
        java.util.regex.Matcher m = p.matcher(s);
        while (m.find()) n++;
        return n;
    }

}
