package com.saraiev.yandexuslugiscraper.utils;

import okhttp3.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;


@Service
public class Downloader {

    private final OkHttpClient client;

    public Downloader(OkHttpClient client) {
        this.client = client;
    }

    public String get(String url) throws IOException {
        Request request = buildRequest(url);
        Response response = getResponse(request);
        return response.body().string();
    }

    public String post(String url, Map<String, String> params) throws IOException {
        Response response = postResponse(url, params);
        return response.body().string();
    }

    public String post(String url, String body) throws IOException {
        Response response = postResponse(url, body);
        return response.body().string();
    }

    private Response postResponse(String url, String body) throws IOException {
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody requestBody = RequestBody.create(JSON, body);
        Request request = new Request.Builder()
                .url(url)
                .header("Accept", "*application/json")
                .post(requestBody)
                .build();
        return client.newCall(request).execute();
    }

    private Response postResponse(String url, Map<String, String> params) throws IOException {
        FormBody.Builder builder = new FormBody.Builder();
        for (Map.Entry entry : params.entrySet()) {
            builder.add(entry.getKey().toString(), entry.getValue().toString());
        }
        Request request = new Request.Builder()
                .url(url)
                .header("Accept", "*application/json")
                .post(builder.build())
                .build();
        return client.newCall(request).execute();
    }

    private Response getResponse(Request request) throws IOException {
        return client.newCall(request).execute();
    }

    private Request buildRequest(String url) {
        return new Request.Builder()
                .url(url)
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
                .header("Accept-Language", "ru-RU,ru;q=0.9,en-US;q=0.8,en;q=0.7")
                .header("Accept-Encoding", "zip, deflate, br")
                .build();
    }

}
