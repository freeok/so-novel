package com.pcdd.sonovel.dsl;

import com.pcdd.sonovel.dsl.core.StepEngine;
import com.pcdd.sonovel.dsl.executor.ExecutorRegistry;
import com.pcdd.sonovel.dsl.executor.impl.JavaExecutor;
import com.pcdd.sonovel.dsl.executor.impl.JavaScriptExecutor;
import lombok.experimental.UtilityClass;

@UtilityClass
public class DslBootstrap {

    public StepEngine createEngine() {

        ExecutorRegistry registry = new ExecutorRegistry();

        registry.register("java", new JavaExecutor());
        registry.register("js", new JavaScriptExecutor());

        return new StepEngine(registry);
    }

}