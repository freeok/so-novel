package com.pcdd.sonovel.action;

import cn.hutool.core.lang.Console;
import cn.hutool.core.lang.ConsoleTable;
import cn.hutool.log.dialect.console.ConsoleLog;
import cn.hutool.log.level.Level;
import com.pcdd.sonovel.core.Source;
import com.pcdd.sonovel.model.Rule;
import com.pcdd.sonovel.model.SourceInfo;
import lombok.SneakyThrows;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 书源一览
 *
 * @author pcdd
 * Created at 2025/1/9
 */
public class ShowSourcesAction {

    static {
        ConsoleLog.setLevel(Level.OFF);
    }

    public void execute() {
        Console.log("<== Please wait ...");

        List<Rule> list = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            Source source = new Source(i);
            list.add(source.rule);
        }

        ConsoleTable asciiTables = ConsoleTable.create()
                .setSBCMode(false)
                .addHeader("ID", "书源", "延迟", "状态码");
        testWebsiteDelays(list).forEach(e -> asciiTables.addBody(
                e.getId() + "",
                e.getName(),
                e.getDelay() + " ms",
                e.getCode() + ""));
        Console.table(asciiTables);
    }

    @SneakyThrows
    private static List<SourceInfo> testWebsiteDelays(List<Rule> rules) {
        List<SourceInfo> res = new ArrayList<>();
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        CompletionService<SourceInfo> completionService = new ExecutorCompletionService<>(executorService);

        for (Rule r : rules) {
            completionService.submit(() -> {
                SourceInfo source = new SourceInfo();
                source.setId(r.getId());
                source.setName(r.getName());
                long startTime = System.currentTimeMillis();
                try {
                    URL url = new URL(r.getUrl());
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setConnectTimeout(3_000);
                    conn.setReadTimeout(3_000);
                    conn.connect();
                    source.setDelay((int) (System.currentTimeMillis() - startTime));
                    source.setCode(conn.getResponseCode());
                } catch (Exception e) {
                    source.setDelay(-1);  // 出现异常时设置为负值或其他标记
                }
                return source;
            });
        }

        // 获取任务结果并按延迟排序
        for (int i = 0; i < rules.size(); i++) {
            // 获取最先完成的任务的结果
            res.add(completionService.take().get());
        }

        executorService.shutdown();

        // 按照延迟排序
        res.sort((o1, o2) -> {
            int delay1 = o1.getDelay();
            int delay2 = o2.getDelay();

            if (delay1 < 0) delay1 = Integer.MAX_VALUE;
            if (delay2 < 0) delay2 = Integer.MAX_VALUE;

            return Integer.compare(delay1, delay2);
        });

        return res;
    }

}