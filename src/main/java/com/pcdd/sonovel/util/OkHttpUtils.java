package com.pcdd.sonovel.util;

import lombok.experimental.UtilityClass;
import okhttp3.*;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author pcdd
 * Created at 2025/4/20
 */
@UtilityClass
public class OkHttpUtils {

    private final OkHttpClient client;
    public final int TIMEOUT = 5000;

    // 全局共享的单例模式
    static {
        client = createClient();
    }

    // 创建 OkHttpClient 并强制 TLS 1.2
    public OkHttpClient createClient() {
        ConnectionSpec spec = new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                .tlsVersions(TlsVersion.TLS_1_2) // 强制使用 TLS 1.2
                .allEnabledCipherSuites() // 启用所有密码套件
                .build();

        return new OkHttpClient.Builder()
                .connectionSpecs(List.of(spec, ConnectionSpec.CLEARTEXT)) // 兼容 HTTP
                .connectTimeout(TIMEOUT, TimeUnit.MILLISECONDS)  // 设置连接超时时间
                .readTimeout(TIMEOUT, TimeUnit.MILLISECONDS)    // 设置读取超时时间
                .build();
    }

    // 跳过 SSL 验证
    public static OkHttpClient createUnsafeClient() throws Exception {
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public void checkClientTrusted(X509Certificate[] chain, String authType) {
                    }

                    public void checkServerTrusted(X509Certificate[] chain, String authType) {
                    }

                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[]{};
                    }
                }
        };

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

        return new OkHttpClient.Builder()
                .sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager) trustAllCerts[0])
                .hostnameVerifier((hostname, session) -> true)
                .connectTimeout(TIMEOUT, TimeUnit.MILLISECONDS)
                .readTimeout(TIMEOUT, TimeUnit.MILLISECONDS)
                .build();
    }

    // 发送 GET 请求
    public static String get(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }
            return response.body() != null ? response.body().string() : null;
        }
    }

    // 发送 POST 请求 (参数为 JSON 格式)
    public static String post(String url, String json) throws IOException {
        RequestBody body = RequestBody.create(json, MediaType.parse("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }
            return response.body() != null ? response.body().string() : null;
        }
    }

    // 发送 POST 请求 (表单数据)
    public static String post(String url, FormBody formBody) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }
            return response.body() != null ? response.body().string() : null;
        }
    }

}