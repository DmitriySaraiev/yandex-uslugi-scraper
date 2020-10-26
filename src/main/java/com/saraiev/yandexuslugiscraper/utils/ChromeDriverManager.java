package com.saraiev.yandexuslugiscraper.utils;

import com.saraiev.yandexuslugiscraper.domain.ChromeDriverWrapper;
import com.saraiev.yandexuslugiscraper.domain.SourceCookiesWraper;
import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.RasterFormatException;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Service
public class ChromeDriverManager {

    private final static Logger logger = LoggerFactory.getLogger(ChromeDriverManager.class);

    private final List<ChromeDriverWrapper> chromeDriverWrappers;

    private int captchaImageIndex = 0;

    @Autowired
    private CaptchaSolver captchaSolver;


    public ChromeDriverManager(@Value("${chromedriver.number}") int numberOfDrivers) {
        chromeDriverWrappers = new ArrayList<>();
        WebDriverManager.chromedriver().setup();
        for (int i = 0; i < numberOfDrivers; i++) {
            ChromeOptions chromeOptions = new ChromeOptions();
            chromeOptions.setPageLoadStrategy(PageLoadStrategy.EAGER);
            ChromeDriver chromeDriver = new ChromeDriver(chromeOptions);
            ChromeDriverWrapper chromeDriverWrapper = new ChromeDriverWrapper();
            chromeDriverWrapper.setChromeDriver(chromeDriver);
            chromeDriverWrappers.add(chromeDriverWrapper);
        }
    }

    @SneakyThrows
    private ChromeDriverWrapper getFreeChromeDriver() {
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

    public SourceCookiesWraper getPageSource(String url) {
        ChromeDriverWrapper chromeDriverWrapper = getFreeChromeDriver();
        try {
            chromeDriverWrapper.setBusy(true);
            ChromeDriver chromeDriver = chromeDriverWrapper.getChromeDriver();
            chromeDriver.get(url);
            waitForCaptchaSolution(chromeDriver);
            SourceCookiesWraper sourceCookiesWraper = new SourceCookiesWraper();
            sourceCookiesWraper.setCookies(chromeDriver.manage().getCookies());
            sourceCookiesWraper.setSource(chromeDriver.findElement(By.tagName("html")).getAttribute("outerHTML"));
            return sourceCookiesWraper;
        } finally {
            chromeDriverWrapper.setBusy(false);
        }
    }


    @SneakyThrows
    private void waitForCaptchaSolution(ChromeDriver chromeDriver) {
        while (true) {
            String source;
            while (true) {
                try {
                    source = chromeDriver.findElement(By.tagName("body")).getAttribute("outerHTML");
                } catch (NoSuchElementException e) {
                    Thread.sleep(2000);
                    continue;
                }
                break;
            }
            if (source.contains("Нам очень жаль, но запросы, поступившие с вашего IP-адреса")) {
                logger.info("captcha");

                WebElement webElement;
                File screenshot;

                BufferedImage fullImg;
                Point point;
                int eleWidth;
                int eleHeight;
                BufferedImage eleScreenshot;

                while (true) {
                    try {
                        webElement = chromeDriver.findElement(By.cssSelector("img"));
                        screenshot = ((TakesScreenshot) chromeDriver).getScreenshotAs(OutputType.FILE);
                        fullImg = ImageIO.read(screenshot);
                        point = webElement.getLocation();
                        eleWidth = webElement.getSize().getWidth();
                        eleHeight = webElement.getSize().getHeight();
                        eleScreenshot = fullImg.getSubimage(point.getX(), point.getY(),
                                eleWidth, eleHeight);
                    } catch (RasterFormatException e) {
                        Thread.sleep(3000);
                        continue;
                    }
                    break;
                }
                ImageIO.write(eleScreenshot, "png", screenshot);

                String captchaImageFileName = String.format("capthcha%s.png", captchaImageIndex++);

                File screenshotLocation = new File(captchaImageFileName);
                FileUtils.copyFile(screenshot, screenshotLocation);
                String captchaSolveResult = captchaSolver.solveImageCaptcha(captchaImageFileName);
                if (captchaSolveResult == null || captchaSolveResult.equals("")) {
                    continue;
                }
                WebElement responseElement = chromeDriver.findElement(By.cssSelector("div.input-wrapper__input-field input"));
                responseElement.sendKeys(captchaSolveResult);
                WebElement submitElement = chromeDriver.findElement(By.cssSelector("button.submit"));
                submitElement.click();
            } else {
                break;
            }
        }
    }

    @PreDestroy
    public void quitAllDriver() {
        chromeDriverWrappers.parallelStream().forEach(chromeDriverWrapper -> chromeDriverWrapper.getChromeDriver().quit());
    }

}
