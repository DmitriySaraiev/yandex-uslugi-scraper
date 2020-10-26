package com.saraiev.yandexuslugiscraper.utils;

import okhttp3.*;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.Cookie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;


@Service
public class Downloader {

    private final OkHttpClient client;

    @Autowired
    private ChromeDriverManager chromeDriverManager;

    public Downloader(OkHttpClient client) {
        this.client = client;
    }

    public String get(String url) throws IOException {
        Request request = buildRequest(url);
        Response response = getResponse(request);
        return response.body().string();
    }

    public String get(String url, Set<Cookie> cookies) throws IOException {
        Request request = buildRequest(url, cookies);
        Response response = getResponse(request);
        return response.body().string();
    }

    public String post(String url, String body, Set<Cookie> cookies) throws IOException {
        Response response = postResponse(url, body, cookies);
        return response.body().string();
    }

    public String post(String url, String body) throws IOException {
        Response response = postResponse(url, body);
        return response.body().string();
    }

    private Response postResponse(String url, String body, Set<Cookie> cookies) throws IOException {
        Iterator<Cookie> iterator = cookies.iterator();
        StringBuilder cookieSb = new StringBuilder();
        while (iterator.hasNext()) {
            Cookie cookie = (Cookie) iterator.next();
            cookieSb.append(cookie.getName() + "=" + cookie.getValue() + "; ");
        }
        String cookie = cookieSb.toString();
        cookie = cookie.substring(0, cookie.length() - 2);
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody requestBody = RequestBody.create(JSON, body);
        Request request = new Request.Builder()
                .url(url)
                .header("Accept", "*application/json")
                .header("Cookie", cookie)
                .post(requestBody)
                .build();
        return client.newCall(request).execute();
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


    private Response getResponse(Request request) throws IOException {
        return client.newCall(request).execute();
    }

    private Request buildRequest(String url) {
        return new Request.Builder()
                .url(url)
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
                .header("Accept-Language", "ru-RU,ru;q=0.9,en-US;q=0.8,en;q=0.7")
                .header("Accept-Encoding", "zip, deflate, br")
                .header("authority", "yandex.ru")
                .header("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.111 Safari/537.36z")
                .build();
    }

    private Request buildRequest(String url, Set<Cookie> cookies) {
        Iterator<Cookie> iterator = cookies.iterator();
        StringBuilder cookieSb = new StringBuilder();
        while (iterator.hasNext()) {
            Cookie cookie = (Cookie) iterator.next();
            if (cookie.getName().equals("_ym_wasSynced") || StringUtils.startsWith(cookie.getName(), "_ym_visorc_")) {
                continue;
            }
            cookieSb.append(cookie.getName() + "=" + cookie.getValue() + "; ");
        }
        String cookie = cookieSb.toString();
        cookie = cookie.substring(0, cookie.length() - 2);
        return new Request.Builder()
                .url(url)
//                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
//                .header("Accept-Language", "ru-RU,ru;q=0.9,en-US;q=0.8,en;q=0.7")
//                .header("Accept-Encoding", "gzip, deflate, br")
                .header("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.111 Safari/537.36")
                .header("Cookie", cookie)
                .header("authority", "yandex.ru")
                .header("cache-control", "max-age=0")
                .header("sec-fetch-site", "none")
                .header("sec-fetch-mode", "navigate")
                .header("sec-fetch-user", "?1")
                .header("sec-sec-dest", "document")
                .header("Connection", "keep-alive")
                .header("upgrade-insecure-requests", "1")
//                .header("Cookie", cookie.substring(0, cookie.length() - 2))
                .build();
    }

}
