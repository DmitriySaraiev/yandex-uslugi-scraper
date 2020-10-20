package com.saraiev.yandexuslugiscraper.utils;

import lombok.Data;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Base64;

@Service
@Data
public class CaptchaSolver {

    private final static Logger logger = LoggerFactory.getLogger(CaptchaSolver.class);

    private String anticaptchaApiKey;

    private boolean isSolvingInProcess = false;

    private final Downloader downloader;

    public CaptchaSolver(Downloader downloader, @Value("${anticaptcha.api.key}") String anticaptchaApiKey) {
        this.downloader = downloader;
        this.anticaptchaApiKey = anticaptchaApiKey;
    }

    @SneakyThrows
    public String solveImageCaptcha() {

        byte[] fileContent = FileUtils.readFileToByteArray(new File("capthca.png"));
        String encodedString = Base64.getEncoder().encodeToString(fileContent);

        String body = "{\n" +
                "    \"clientKey\": \"" + anticaptchaApiKey + "\",\n" +
                "    \"task\": {\n" +
                "        \"type\": \"ImageToTextTask\",\n" +
                "        \"body\": \"" + encodedString + "\",\n" +
                "        \"phrase\": false,\n" +
                "        \"case\": false,\n" +
                "        \"numeric\": false,\n" +
                "        \"math\": 0,\n" +
                "        \"minLength\": 0,\n" +
                "        \"maxLength\": 0\n" +
                "    }," +
                " \"languagePool\": \"rn\"\n" +
                "}";

        String postResponse = downloader.post("https://api.anti-captcha.com/createTask ", body);

        while (postResponse.contains("ERROR_NO_SLOT_AVAILABLE")) {
            Thread.sleep(3000);
            logger.info("No captha workers available. Waiting.");
            postResponse = downloader.post("https://api.anti-captcha.com/createTask ", body);
        }

        String taskId = StringUtils.substringBetween(postResponse, "taskId\":", "}");
        body = "{\n" +
                "    \"clientKey\": \"" + anticaptchaApiKey + "\",\n" +
                "    \"taskId\": \"" + taskId + "\"" +
                "}";

        postResponse = downloader.post("https://api.anti-captcha.com/getTaskResult", body);
        while (postResponse.contains("processing")) {
            logger.info("waiting for capthcha solution");
            Thread.sleep(4000);
            postResponse = downloader.post("https://api.anti-captcha.com/getTaskResult", body);
        }
        if (StringUtils.substringBetween(postResponse, "text\":\"", "\",") == null)
            logger.info("error solving capthca");
        return StringUtils.substringBetween(postResponse, "text\":\"", "\",");
    }

}
