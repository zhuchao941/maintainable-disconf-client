package com.github.zhuchao941.disconf.client.util;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import okhttp3.ConnectionPool;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by @author zhuchao on @date 2019/2/12.
 */
public class HttpUtils {

    public final static Duration CONNECT_TIMEOUT = Duration.ofMillis(1200L);
    public final static Duration READ_TIMEOUT = Duration.ofMillis(1500L);
    public final static Duration CALL_TIMEOUT = Duration.ofMillis(2000L);

    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    public final static OkHttpClient CLIENT = new OkHttpClient.Builder()
            .connectTimeout(CONNECT_TIMEOUT).readTimeout(READ_TIMEOUT).callTimeout(CALL_TIMEOUT)
            .connectionPool(new ConnectionPool(10, 5, TimeUnit.MINUTES)).build();

    public static String get(String url) throws IOException {
        Request request = new Request.Builder().url(url).build();
        try (Response response = CLIENT.newCall(request).execute()) {
            return response.body().string();
        }
    }

    public static String postJsonBody(String url, String json) throws IOException {
        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder().url(url).post(body).build();
        try (Response response = CLIENT.newCall(request).execute()) {
            return response.body().string();
        }
    }
}
