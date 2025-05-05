package com.pcdd.sonovel.util;

import lombok.experimental.UtilityClass;
import okhttp3.OkHttpClient;

@UtilityClass
public class HttpClientContext {

    private static final InheritableThreadLocal<OkHttpClient> current = new InheritableThreadLocal<>();

    public void set(OkHttpClient client) {
        current.set(client);
    }

    public OkHttpClient get() {
        return current.get();
    }

    public void clear() {
        current.remove();
    }

}