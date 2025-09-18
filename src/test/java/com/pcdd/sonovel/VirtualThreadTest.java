package com.pcdd.sonovel;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.stream.IntStream;

class VirtualThreadTest {

    private static final int MAX_CONCURRENT = 50; // 限制最大并发 50
    private static final Semaphore SEMAPHORE = new Semaphore(MAX_CONCURRENT);

    @Test
    void testLimiter() {
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 1; i <= 200; i++) {
                int taskId = i;
                executor.submit(() -> {
                    try {
                        SEMAPHORE.acquire(); // 限流
                        System.out.println("执行任务 " + taskId + "，线程: " + Thread.currentThread());
                        Thread.sleep(1000); // 模拟耗时任务
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        SEMAPHORE.release();
                    }
                });
            }
        }
    }

    @Test
    void testStartTime() throws Exception {
        Set<String> poolNames = ConcurrentHashMap.newKeySet();
        Set<String> pThreadsNames = ConcurrentHashMap.newKeySet();
        List<Thread> threads = IntStream.range(0, 10_000_000).mapToObj(i -> Thread.ofVirtual().unstarted(() -> {
            poolNames.add(readPoolName());
            pThreadsNames.add(readWorkerName());
        })).toList();
        Instant begin = Instant.now();
        threads.forEach(Thread::start);
        for (Thread thread : threads) {
            thread.join();
        }
        Instant end = Instant.now();
        System.out.println("Time = " + Duration.between(begin, end).toMillis() + "ms");
        System.out.println("# core =" + Runtime.getRuntime().availableProcessors());
        System.out.println("# Pools: " + poolNames.size());
        System.out.println("# Platform threads:" + pThreadsNames.size());
    }

    private static String readPoolName() {
        String name = Thread.currentThread().toString();
        return name.substring(name.indexOf("@ForkJoinPool"), name.indexOf("worker"));
    }

    private static String readWorkerName() {
        String name = Thread.currentThread().toString();
        return name.substring(name.indexOf("worker"));
    }

}