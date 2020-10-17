package com.saraiev.yandexuslugiscraper.utils;

import com.saraiev.yandexuslugiscraper.domain.ChromeDriverWrapper;
import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.SneakyThrows;
import org.openqa.selenium.chrome.ChromeDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;

@Service
public class ChromeDriverProvider {

    private final static Logger logger = LoggerFactory.getLogger(ChromeDriverProvider.class);

    private final List<ChromeDriverWrapper> chromeDriverWrappers;

    public ChromeDriverProvider(@Value("${chromedriver.number}") int numberOfDrivers) {
        chromeDriverWrappers = new ArrayList<>();
        WebDriverManager.chromedriver().setup();
        for (int i = 0; i < numberOfDrivers; i++) {
            ChromeDriver chromeDriver = new ChromeDriver();
            ChromeDriverWrapper chromeDriverWrapper = new ChromeDriverWrapper();
            chromeDriverWrapper.setChromeDriver(chromeDriver);
            chromeDriverWrappers.add(chromeDriverWrapper);
        }
    }

    @SneakyThrows
    public ChromeDriverWrapper getFreeChromeDriver() {
        while (true) {
            for (ChromeDriverWrapper chromeDriverWrapper : chromeDriverWrappers) {
                if (!chromeDriverWrapper.isBusy()) {
                    return chromeDriverWrapper;
                }
            }
            logger.info("all chromedrivers are busy, waiting...");
            Thread.sleep(2000);
        }
    }

    @PreDestroy
    public void quitAllDriver() {
        chromeDriverWrappers.parallelStream().forEach(chromeDriverWrapper -> chromeDriverWrapper.getChromeDriver().quit());
    }
}
