package com.pcdd.sonovel.utils;

import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 搜索频率间隔提示检测 {@link CrawlUtils#detectSearchInterval(String)} 的分支测试（纯单元，无网络）。
 * <p>
 * 笔趣阁系 CMS（如少年小说网）两次搜索间隔不足时返回 HTTP 200，结果区只有提示文本，
 * 正则：搜索间隔【n】秒，请于 HH:mm:ss 后再进行搜索。
 */
class CrawlUtilsIntervalTest {

    static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    static String hintHtml(int interval, LocalTime target) {
        return "<html>搜索间隔【" + interval + "】秒，请于 "
                + target.format(FMT) + " 后再进行搜索！</html>";
    }

    /** blank / null HTML：无提示，返回 0 */
    @Test
    void blankOrNullHtml_returnsZero() {
        assertEquals(0L, CrawlUtils.detectSearchInterval(""));
        assertEquals(0L, CrawlUtils.detectSearchInterval(null));
    }

    /** 普通页面（无间隔提示）：返回 0 */
    @Test
    void noHint_returnsZero() {
        assertEquals(0L, CrawlUtils.detectSearchInterval("<html>普通搜索结果页</html>"));
    }

    /**
     * 目标时间已过 → 时钟差为负；若恰好跨午夜则时钟差为超大正值。
     * 两种异常时钟都应回退到完整间隔 + 2s。
     */
    @Test
    void pastTarget_fallsBackToIntervalPlus2() {
        int interval = 60;
        String html = hintHtml(interval, LocalTime.now().minusSeconds(30));
        assertEquals(interval + 2L, CrawlUtils.detectSearchInterval(html));
    }

    /**
     * 目标时间在近未来（now + 10s）→ 按本地时钟差等待（约 12s）；
     * 若恰好跨越午夜则回退 interval + 2。
     */
    @Test
    void nearFutureTarget_returnsClockWait() {
        int interval = 60;
        String html = hintHtml(interval, LocalTime.now().plusSeconds(10));
        long wait = CrawlUtils.detectSearchInterval(html);
        // 正常：约 10+2=12s（毫秒误差给 [10,14] 容差）；跨午夜：回退 62s
        assertTrue((wait >= 10 && wait <= 14) || wait == interval + 2L,
                "unexpected wait seconds: " + wait);
    }

    /** 笔趣阁365 式 alert 提示（无【】、无目标时钟）：alert('搜索间隔15秒,请稍后重新搜索！') → 完整间隔 15+2 */
    @Test
    void alertStyleHint_returnsIntervalPlus2() {
        String html = "<script>alert('搜索间隔15秒,请稍后重新搜索！')</script>";
        assertEquals(17L, CrawlUtils.detectSearchInterval(html));
    }

    /** 通用"搜索间隔 N 秒"（无括号、无时钟）：→ N+2 */
    @Test
    void genericIntervalHint_returnsIntervalPlus2() {
        assertEquals(22L, CrawlUtils.detectSearchInterval("<html>搜索间隔20秒，请稍后再试</html>"));
    }

}
