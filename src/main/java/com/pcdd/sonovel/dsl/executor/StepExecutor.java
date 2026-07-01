package com.pcdd.sonovel.dsl.executor;

@FunctionalInterface
public interface StepExecutor {

    String execute(String input, String code);

}