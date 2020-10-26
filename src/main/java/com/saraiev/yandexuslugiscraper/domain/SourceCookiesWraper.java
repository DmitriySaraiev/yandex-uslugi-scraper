package com.saraiev.yandexuslugiscraper.domain;

import lombok.Data;
import org.openqa.selenium.Cookie;

import java.util.Set;

@Data
public class SourceCookiesWraper {

    private String source;
    private Set<Cookie> cookies;

}
