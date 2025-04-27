package com.pcdd.sonovel.action;

import cn.hutool.core.lang.Console;
import cn.hutool.core.lang.ConsoleTable;
import com.pcdd.sonovel.core.Source;
import com.pcdd.sonovel.model.Rule;
import com.pcdd.sonovel.model.SourceInfo;
import com.pcdd.sonovel.util.ConfigUtils;
import com.pcdd.sonovel.util.OkHttpUtils;
import com.pcdd.sonovel.util.RandomUA;
import com.pcdd.sonovel.util.SourceUtils;
import lombok.SneakyThrows;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.jline.jansi.AnsiRenderer.render;

/**
 * 书源一览
 *
 * @author pcdd
 * Created at 2025/1/9
 */
public class ShowSourcesAction {

    public void execute() {
        Console.log("<== 测试延迟中 ...");

        ConsoleTable asciiTables = ConsoleTable.create()
                .setSBCMode(false)
                .addHeader("ID", "书源", "延迟", "状态码", "URL");
        List<Rule> rules = SourceUtils.ALL_IDS.stream()
                .map(id -> new Source(id).rule)
                .toList();

        testWebsiteDelays(rules).forEach(e -> asciiTables.addBody(
                e.getId() + "",
                e.getName(),
                e.getDelay() + " ms",
                e.getCode() + "",
                e.getUrl())
        );

        Console.table(asciiTables);
    }

    @SneakyThrows
    private static List<SourceInfo> testWebsiteDelays(List<Rule> rules) {
        List<SourceInfo> res = new ArrayList<>();
        ExecutorService threadPool = Executors.newFixedThreadPool(rules.size());
        CompletionService<SourceInfo> completionService = new ExecutorCompletionService<>(threadPool);
        OkHttpClient client = OkHttpUtils.createClient(ConfigUtils.config(), true);

        for (Rule r : rules) {
            completionService.submit(() -> {
                SourceInfo source = SourceInfo.builder()
                        .id(r.getId())
                        .name(r.getName())
                        .url(r.getUrl())
                        .build();
                try {
                    Request req = new Request.Builder()
                            .url(r.getUrl())
                            .header("User-Agent", RandomUA.generate())
                            .head() // 只发 HEAD 请求，不获取 body，更快！
                            .build();
                    // 放这里才最准确
                    long startTime = System.currentTimeMillis();
                    try (Response resp = client.newCall(req).execute()) {
                        source.setDelay((int) (System.currentTimeMillis() - startTime));
                        source.setCode(resp.code());
                    }
                } catch (Exception e) {
                    source.setDelay(-1);
                    source.setCode(-1);
                    if (System.getProperty("env").equalsIgnoreCase("dev")) {
                        Console.error(render("书源 {} 【{}】 测试延迟异常：{}", "red"), r.getId(), r.getName(), e.getMessage());
                    }
                }

                return source;
            });
        }

        for (int i = 0; i < rules.size(); i++) {
            // 获取最先完成的任务的结果
            res.add(completionService.take().get());
        }

        threadPool.shutdown();

        res.sort((o1, o2) -> {
            int delay1 = o1.getDelay() < 0 ? Integer.MAX_VALUE : o1.getDelay();
            int delay2 = o2.getDelay() < 0 ? Integer.MAX_VALUE : o2.getDelay();
            return Integer.compare(delay1, delay2);
        });

        return res;
    }

}