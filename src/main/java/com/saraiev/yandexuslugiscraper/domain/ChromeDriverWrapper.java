package com.saraiev.yandexuslugiscraper.domain;

import lombok.Data;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.chrome.ChromeDriver;

import java.util.HashSet;
import java.util.Set;

@Data
public class ChromeDriverWrapper {

    private ChromeDriver chromeDriver;
    private boolean isBusy;
    private String lastUrl;

}
