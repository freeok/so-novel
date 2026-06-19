package com.pcdd.sonovel.util;

import java.util.concurrent.*;

/**
 * 使用 Semaphore 控制虚拟线程并发量的工具类
 *
 * @author pcdd
 * Created at 2025/9/18
 */
public class VirtualThreadLimiter implements AutoCloseable {

    private final Semaphore semaphore;
    private final ExecutorService executor;
    private final Phaser phaser = new Phaser(1);

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
        phaser.register();

        executor.submit(() -> {
            try {
                semaphore.acquire();
                task.run();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                semaphore.release();
                phaser.arriveAndDeregister();
            }
        });
    }

    /**
     * 优雅关闭
     */
    @Override
    public void close() {
        phaser.arriveAndAwaitAdvance();

        executor.shutdown();

        try {
            if (!executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

}