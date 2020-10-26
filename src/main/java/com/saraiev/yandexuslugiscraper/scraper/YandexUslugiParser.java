package com.saraiev.yandexuslugiscraper.scraper;

import com.saraiev.yandexuslugiscraper.domain.ServiceProvider;
import com.saraiev.yandexuslugiscraper.utils.Downloader;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.Cookie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class YandexUslugiParser {

    private final static Logger logger = LoggerFactory.getLogger(YandexUslugiParser.class);

    private final Downloader downloader;

    public YandexUslugiParser(Downloader downloader) {
        this.downloader = downloader;
    }

    public ServiceProvider parseServiceProvider(Document document, ServiceProvider serviceProvider, Set<Cookie> cookies) {

        String name = null;
        String phone;
        String email = null;
        String vkUrl = null;
        String instagramUrl = null;
        String facebookUrl = null;
        String youtubeUrl = null;
        String profiRu = null;
        boolean passportChecked;
        Float overallRating = null;
        String location = null;

        Element nameEl = document.selectFirst("div.WorkerAbout2-Info b");
        if (nameEl != null) {
            name = nameEl.text();
        }
        phone = requestPhone(document, cookies);
        if (phone == null || phone.equals("")) {
            logger.error("no phone found for {}", serviceProvider.getUrl());
        }
        Elements socialLinksElems = document.select("a.WorkerAbout2-SocialLink");
        for (Element socialLinksElem : socialLinksElems) {
            if (socialLinksElem.attr("href").contains("vk.com")) {
                vkUrl = socialLinksElem.attr("href");
            } else if (socialLinksElem.attr("href").contains("instagram")) {
                instagramUrl = socialLinksElem.attr("href");
            } else if (socialLinksElem.attr("href").contains("facebook")) {
                facebookUrl = socialLinksElem.attr("href");
            } else if (socialLinksElem.attr("href").contains("youtube")) {
                youtubeUrl = socialLinksElem.attr("href");
            } else if (socialLinksElem.attr("href").contains("profi.ru")) {
                profiRu = socialLinksElem.attr("href");
            }
        }

        Element passportCheckedEl = document.selectFirst("span:contains(Паспорт проверен)");
        passportChecked = passportCheckedEl == null ? false : true;
        Element overallRatingEl = document.selectFirst("b.GreenRating-Rating");
        if (overallRatingEl != null) {
            overallRating = Float.parseFloat(overallRatingEl.text());
        }
        Element locationEl = document.selectFirst("div.WorkerMap-Addresses");
        if (locationEl != null) {
            location = locationEl.text();
        }
        Element emailEl = document.selectFirst("a.SocialLinkList-public_email");
        if (emailEl != null) {
            email = StringUtils.substringAfter(emailEl.attr("href"), "mailto:");
        }

        serviceProvider.setName(name);
        serviceProvider.setVk(vkUrl);
        serviceProvider.setProfiru(profiRu);
        serviceProvider.setInstagram(instagramUrl);
        serviceProvider.setFacebook(facebookUrl);
        serviceProvider.setYoutube(youtubeUrl);
        serviceProvider.setPhone(phone);
        serviceProvider.setEmail(email);
        serviceProvider.setIsPassportVerified(passportChecked);
        serviceProvider.setRating(overallRating);
        serviceProvider.setLocation(location);
        serviceProvider.setParsed(true);

        return serviceProvider;
    }

    @SneakyThrows
    private String requestPhone(Document document, Set<Cookie> cookies) {
        try {
            String id = StringUtils.substringBetween(document.html(), "\"workerIds\":[\"", "\"],\"blender\"");
            String apiUrl = "https://yandex.ru/uslugi/api/get_worker_phone?ajax=1";
            String body = String.format("{\n" +
                    "    \"data\": {\n" +
                    "        \"params\": {\n" +
                    "            \"id\": \"%s\"\n" +
                    "        }\n" +
                    "    }\n" +
                    "}", id);
            String phoneResponse = downloader.post(apiUrl, body, cookies);
            return StringUtils.substringBetween(phoneResponse, "phone\":\"", "\"},");
        } catch (Exception e) {
            return null;
        }
    }

}
