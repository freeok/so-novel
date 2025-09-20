package com.pcdd.sonovel.core;

import cn.hutool.core.lang.Console;
import com.pcdd.sonovel.model.AppConfig;
import com.pcdd.sonovel.util.EnvUtils;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import okhttp3.ConnectionSpec;
import okhttp3.OkHttpClient;
import okhttp3.Request;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author pcdd
 * Created at 2025/4/20
 */
@UtilityClass
public class OkHttpClientFactory {

    public final int TIMEOUT = 10;

    public OkHttpClient create() {
        return create(null, false);
    }

    public OkHttpClient create(AppConfig config) {
        return create(config, false);
    }

    /**
     * 创建 OkHttpClient
     *
     * @param config 配置文件
     * @param unsafe 是否跳过 SSL 验证
     */
    @SneakyThrows
    public OkHttpClient create(AppConfig config, boolean unsafe) {
        if (EnvUtils.isDev()) {
            Console.log("com.pcdd.sonovel.core.OkHttpClientFactory#create (unsafe=%s)".formatted(unsafe));
        }

        OkHttpClient.Builder builder = baseBuilder(config);

        if (unsafe) {
            applyUnsafeSsl(builder);
        }

        return builder.build();
    }

    private OkHttpClient.Builder baseBuilder(AppConfig config) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectionSpecs(List.of(
                        ConnectionSpec.MODERN_TLS,
                        ConnectionSpec.COMPATIBLE_TLS, // 兼容老 TLS 网站
                        ConnectionSpec.CLEARTEXT       // 支持 HTTP 明文
                ))
                .connectTimeout(TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(TIMEOUT, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .followRedirects(true) // 自动跟随 301/302 跳转
                .followSslRedirects(true)
                .addInterceptor(chain -> {
                    Request original = chain.request();
                    Request req = original.newBuilder()
                            .header("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8")
                            .build();
                    return chain.proceed(req);
                });

        if (config != null && config.getProxyEnabled() == 1) {
            builder.proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(config.getProxyHost(), config.getProxyPort())));
        }
        return builder;
    }

    /**
     * 跳过 SSL 验证
     */
    @SneakyThrows
    private void applyUnsafeSsl(OkHttpClient.Builder builder) {
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public void checkClientTrusted(X509Certificate[] chain, String authType) {
                    }

                    public void checkServerTrusted(X509Certificate[] chain, String authType) {
                    }

                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }
                }
        };
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
        builder.sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager) trustAllCerts[0])
                .hostnameVerifier((hostname, session) -> true);
    }

}