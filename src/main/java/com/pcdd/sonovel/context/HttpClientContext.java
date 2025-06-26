package com.pcdd.sonovel.context;

import lombok.experimental.UtilityClass;
import okhttp3.OkHttpClient;

@UtilityClass
public class HttpClientContext {

    private final InheritableThreadLocal<OkHttpClient> current = new InheritableThreadLocal<>();

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