package com.pcdd.sonovel.web;

import java.io.PrintStream;

public class ConsoleOutputInterceptor {
    private static MulticastOutputStream multicastStream;

    public static void install() {
        if (multicastStream != null) return;

        // 创建多路输出流，包装原始System.out
        multicastStream = new MulticastOutputStream(System.out);

        // 替换System.out（保留原始功能）
        System.setOut(new PrintStream(multicastStream, true));
    }

    public static void addListener(ConsoleOutputListener listener) {
        if (multicastStream == null) install();
        multicastStream.addListener(listener);
    }

    public static void removeListener(ConsoleOutputListener listener) {
        if (multicastStream != null) {
            multicastStream.removeListener(listener);
        }
    }
}
