package com.pcdd.sonovel.dsl.core;

import com.pcdd.sonovel.dsl.executor.ExecutorRegistry;
import com.pcdd.sonovel.dsl.executor.StepExecutor;

public class StepEngine {

    private final ExecutorRegistry registry;

    public StepEngine(ExecutorRegistry registry) {
        this.registry = registry;
    }

    public String run(String input, String initialContext) {

        DSLParser.Result parsed = DSLParser.parse(input);

        String result = initialContext != null
                ? initialContext
                : parsed.init.trim();

        for (Step step : parsed.steps) {
            StepExecutor executor = registry.get(step.lang);
            result = executor.execute(result, step.code);
        }

        return result;
    }

}