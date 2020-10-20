package com.saraiev.yandexuslugiscraper.utils;

import com.saraiev.yandexuslugiscraper.domain.ChromeDriverWrapper;
import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Service
public class ChromeDriverManager {

    private final static Logger logger = LoggerFactory.getLogger(ChromeDriverManager.class);

    private final List<ChromeDriverWrapper> chromeDriverWrappers;

    private final CaptchaSolver captchaSolver;

    public ChromeDriverManager(@Value("${chromedriver.number}") int numberOfDrivers, CaptchaSolver captchaSolver) {
        this.captchaSolver = captchaSolver;
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
            logger.info("All chromedrivers are busy, waiting...");
            Thread.sleep(2000);
        }
    }

    public void loadPage(ChromeDriverWrapper chromeDriverWrapper, String url) {
        chromeDriverWrapper.setBusy(true);
        ChromeDriver chromeDriver = chromeDriverWrapper.getChromeDriver();
        chromeDriver.get(url);
        waitForCaptchaSolution(chromeDriver);
    }

    public String getPageSource(ChromeDriverWrapper chromeDriverWrapper, String url) {
        chromeDriverWrapper.setBusy(true);
        ChromeDriver chromeDriver = chromeDriverWrapper.getChromeDriver();
        chromeDriver.get(url);
        waitForCaptchaSolution(chromeDriver);
        return chromeDriver.findElement(By.tagName("html")).getAttribute("outerHTML");
    }

    public String getPageSource(ChromeDriverWrapper chromeDriverWrapper) {
        try {
            ChromeDriver chromeDriver = chromeDriverWrapper.getChromeDriver();
            waitForCaptchaSolution(chromeDriver);
            return chromeDriver.findElement(By.tagName("html")).getAttribute("outerHTML");
        } finally {
            chromeDriverWrapper.setBusy(false);
        }
    }

    @SneakyThrows
    private void waitForCaptchaSolution(ChromeDriver chromeDriver) {
        while (true) {
            String source = chromeDriver.findElement(By.tagName("body")).getAttribute("outerHTML");
            if (source.contains("Нам очень жаль, но запросы, поступившие с вашего IP-адреса")) {
                logger.info("captcha");
                captchaSolver.setSolvingInProcess(true);

                WebElement webElement = chromeDriver.findElement(By.cssSelector("img"));
                File screenshot = ((TakesScreenshot) chromeDriver).getScreenshotAs(OutputType.FILE);
                BufferedImage fullImg = ImageIO.read(screenshot);

                Point point = webElement.getLocation();

                int eleWidth = webElement.getSize().getWidth();
                int eleHeight = webElement.getSize().getHeight();

                BufferedImage eleScreenshot = fullImg.getSubimage(point.getX(), point.getY(),
                        eleWidth, eleHeight);
                ImageIO.write(eleScreenshot, "png", screenshot);

                File screenshotLocation = new File("capthca.png");
                FileUtils.copyFile(screenshot, screenshotLocation);
                String captchaSolveResult = captchaSolver.solveImageCaptcha();
                WebElement responseElement = chromeDriver.findElement(By.cssSelector("div.input-wrapper__input-field input"));
                responseElement.sendKeys(captchaSolveResult);
                WebElement submitElement = chromeDriver.findElement(By.cssSelector("button.submit"));
                submitElement.click();
            } else {
                captchaSolver.setSolvingInProcess(false);
                break;
            }
        }
    }

    @PreDestroy
    public void quitAllDriver() {
        chromeDriverWrappers.parallelStream().forEach(chromeDriverWrapper -> chromeDriverWrapper.getChromeDriver().quit());
    }
}
