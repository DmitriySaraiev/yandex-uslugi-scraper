package com.saraiev.yandexuslugiscraper.domain;

import com.sun.org.apache.xpath.internal.operations.Bool;
import lombok.Data;
import org.openqa.selenium.chrome.ChromeDriver;

@Data
public class ChromeDriverWrapper {

    private ChromeDriver chromeDriver;
    private boolean isBusy;
    private String lastUrl;

}
