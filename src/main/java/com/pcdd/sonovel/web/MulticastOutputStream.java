package com.pcdd.sonovel.web;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class MulticastOutputStream extends OutputStream {
    private final OutputStream original;  // 原始输出流（控制台）
    private final List<ConsoleOutputListener> listeners = new ArrayList<>();
    private final ByteArrayOutputStream lineBuffer = new ByteArrayOutputStream();

    public MulticastOutputStream(OutputStream original) {
        this.original = original;
    }

    public void addListener(ConsoleOutputListener listener) {
        listeners.add(listener);
    }

    public void removeListener(ConsoleOutputListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void write(int b) throws IOException {
        original.write(b);  // 先写入原始流（确保控制台正常输出）

        // 按行分割逻辑
        if (b == '\n') {
            notifyListeners();
        } else {
            lineBuffer.write(b);
        }
    }

    @Override
    public void flush() throws IOException {
        if (lineBuffer.size() > 0) {
            notifyListeners();
        }
        original.flush();
    }

    private void notifyListeners() {
        String line = lineBuffer.toString();
        lineBuffer.reset();
        for (ConsoleOutputListener listener : listeners) {
            listener.onLinePrinted(line);
        }
    }
}