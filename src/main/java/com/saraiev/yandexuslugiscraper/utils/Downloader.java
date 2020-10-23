package com.saraiev.yandexuslugiscraper.utils;

import okhttp3.*;
import org.openqa.selenium.Cookie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
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

    public String getWithCookies(String url) throws IOException {
        Request request = buildRequest(url, chromeDriverManager.getCookies());
        Response response = getResponse(request);
        String resp = response.body().string();
        while (resp.contains("Нам очень жаль, но запросы, поступившие с вашего IP-адреса")) {
            chromeDriverManager.getPageSource(url);
            request = buildRequest(url, chromeDriverManager.getCookies());
            response = getResponse(request);
            resp = response.body().string();
        }
        return resp;
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
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.169 Safari/537.36")
                .build();
    }

    private Request buildRequest(String url, Set<Cookie> cookies) {
        Iterator<Cookie> iterator = cookies.iterator();
        StringBuilder cookieSb = new StringBuilder();
        while (iterator.hasNext()) {
            Cookie cookie = (Cookie) iterator.next();
            cookieSb.append(cookie.getName() + "=" + cookie.getValue() + "; ");
        }
        String cookie = cookieSb.toString();
        cookie = cookie.substring(0, cookie.length() - 2);
//        String cookie = "ar=1603476635595283-422008; _ym_wasSynced=%7B%22time%22%3A1603476630730%2C%22params%22%3A%7B%22eu%22%3A0%7D%2C%22bkParams%22%3A%7B%7D%7D; gdpr=0; _ym_uid=16034766311032835366; _ym_d=1603476631; is_gdpr=0; is_gdpr_b=CIecPxC1CA==; mda=0; yandexuid=4595214421603476636; yuidss=4595214421603476636; i=/0dlmo6U9MKVHoQhkmjIofE1Hd6E1HlUxoe8ceDgQaobvgqoUjSMRDESsyJT4SJ+Z1y7SrBxG9F1hVobhT6X/Q0c68Q=; ymex=1918836636.yrts.1603476636#1918836636.yrtsi.1603476636; _ym_isad=2; _ym_visorc_10630330=b; spravka=dD0xNjAzNDc2NjQzO2k9OTQuMjQyLjU5LjkyO3U9MTYwMzQ3NjY0MzMzNTIyMzI5ODtoPWU1NzBhYTI1YjYzMDU0MWQzYzRiZjkzY2RkYjBlZTU2; _ym_visorc_49540177=w";
        return new Request.Builder()
                .url(url)
//                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
//                .header("Accept-Language", "ru-RU,ru;q=0.9,en-US;q=0.8,en;q=0.7")
//                .header("Accept-Encoding", "gzip, deflate, br")
//                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.169 Safari/537.36")
                .header("Cookie", cookie)
//                .header("Cookie", cookie.substring(0, cookie.length() - 2))
//                .header("Referer", "https://yandex.ru/showcaptcha?cc=1&retpath=https%3A//yandex.ru/uslugi/profile/DmitrijD-940912%3F_c1b864c6735f69f3d098ee04070d7ff8&t=0/1603476634/81c051afbfc77031dc3eeb525b6a3aa9&s=c72fa20658c6c50d75eb8817ae226e99")
                .build();
    }

}
