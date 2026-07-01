package com.pcdd.sonovel.dsl.executor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ExecutorRegistry {

    private final Map<String, StepExecutor> registry = new ConcurrentHashMap<>();

    public void register(String lang, StepExecutor executor) {
        registry.put(lang, executor);
    }

    public StepExecutor get(String lang) {
        StepExecutor executor = registry.get(lang);
        if (executor == null) {
            throw new IllegalArgumentException("No executor for: " + lang);
        }
        return executor;
    }

}