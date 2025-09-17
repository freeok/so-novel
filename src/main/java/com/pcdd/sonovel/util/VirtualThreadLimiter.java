package com.pcdd.sonovel.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * 使用 Semaphore 控制虚拟线程并发量的工具类
 *
 * @author pcdd
 * Created at 2025/9/18
 */
public class VirtualThreadLimiter implements AutoCloseable {

    private final Semaphore semaphore;
    private final ExecutorService executor;

    /**
     * 构造方法
     *
     * @param maxConcurrent 最大并发数
     */
    public VirtualThreadLimiter(int maxConcurrent) {
        this.semaphore = new Semaphore(maxConcurrent);
        this.executor = Executors.newVirtualThreadPerTaskExecutor();
    }

    /**
     * 提交受限任务
     *
     * @param task 待执行的任务
     */
    public void submit(Runnable task) {
        executor.submit(() -> {
            try {
                semaphore.acquire();
                task.run();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                semaphore.release();
            }
        });
    }

    /**
     * 优雅关闭
     */
    @Override
    public void close() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(3, TimeUnit.MINUTES)) {
                System.err.println("仍有任务未完成，强制关闭");
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

}