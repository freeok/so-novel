package com.pcdd.sonovel.dsl.executor.impl;

import com.pcdd.sonovel.dsl.executor.StepExecutor;
import com.pcdd.sonovel.util.JsCaller;

/**
 * @author pcdd
 * Created at 2026/7/1
 */
public class JavaScriptExecutor implements StepExecutor {

    @Override
    public String execute(String input, String code) {
        return JsCaller.call(code, input);
    }

}