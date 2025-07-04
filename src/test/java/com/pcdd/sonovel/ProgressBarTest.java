package com.pcdd.sonovel;

import me.tongfei.progressbar.ProgressBar;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author pcdd
 * Created at 2025/6/26
 */
public class ProgressBarTest {
    public static void main(String[] args) {
        int size = 1500;
        ProgressBar progressBar = ProgressBar.builder()
                .setTaskName("Downloading...")
                .setInitialMax(size)
                .setMaxRenderedLength(100)
                .setUpdateIntervalMillis(100)
                .showSpeed()
                .build();
        ExecutorService executorService = Executors.newFixedThreadPool(20);
        CountDownLatch countDownLatch = new CountDownLatch(size);

        for (int i = 1; i <= size; i++) {
            int finalI = i;
            executorService.execute(() -> {
                progressBar.stepTo(finalI);
                progressBar.setExtraMessage("正在下载第" + finalI + "章");
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } finally {
                    countDownLatch.countDown();
                }
            });
        }

        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        executorService.shutdown();
        progressBar.setExtraMessage("Done!");
        progressBar.close();
    }
}