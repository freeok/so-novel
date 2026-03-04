package com.pcdd.sonovel.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;

/**
 * 使用 Semaphore 控制虚拟线程并发量的工具类
 *
 * @author pcdd
 * Created at 2025/9/18
 */
public class VirtualThreadLimiter implements AutoCloseable {

    private final Semaphore semaphore;
    private final ExecutorService executor;
    private final AtomicInteger taskCount = new AtomicInteger(0);

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
        taskCount.incrementAndGet();

        executor.submit(() -> {
            try {
                semaphore.acquire();
                task.run();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                semaphore.release();
                taskCount.decrementAndGet();
            }
        });
    }

    /**
     * 优雅关闭
     */
    @Override
    public void close() {
        // 等待所有任务执行完成
        while (taskCount.get() > 0) {
            LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(50));
        }

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