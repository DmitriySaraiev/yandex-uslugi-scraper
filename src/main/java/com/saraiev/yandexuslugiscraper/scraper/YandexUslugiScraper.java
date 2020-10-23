package com.saraiev.yandexuslugiscraper.scraper;

import com.saraiev.yandexuslugiscraper.domain.Category;
import com.saraiev.yandexuslugiscraper.domain.CategoryAttribute;
import com.saraiev.yandexuslugiscraper.domain.ServiceProvider;
import com.saraiev.yandexuslugiscraper.service.CategoryAttributeService;
import com.saraiev.yandexuslugiscraper.service.CategoryService;
import com.saraiev.yandexuslugiscraper.service.ServiceProviderService;
import com.saraiev.yandexuslugiscraper.utils.ChromeDriverManager;
import com.saraiev.yandexuslugiscraper.utils.Downloader;
import com.saraiev.yandexuslugiscraper.utils.RegionProvider;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Component
public class YandexUslugiScraper implements CommandLineRunner {

    private final static Logger logger = LoggerFactory.getLogger(YandexUslugiScraper.class);

    private final static String PRIVATE_WORKER_URL_APPENDER = "?wizextra=ydofilters%3Dfeature%3As_features%3Aor%3Aworker_type_private";
    private final static String ORGANIZATION_WORKER_URL_APPENDER = "?wizextra=ydofilters%3Dfeature%3As_features%3Aor%3Aworker_type_org";

    private final CategoryService categoryService;
    private final CategoryAttributeService categoryAttributeService;
    private final ServiceProviderService serviceProviderService;
    private final ChromeDriverManager chromeDriverManager;
    private final YandexUslugiParser yandexUslugiParser;
    private final ExecutorService executorService;
    private final Downloader downloader;

    public YandexUslugiScraper(CategoryService categoryService,
                               CategoryAttributeService categoryAttributeService,
                               ServiceProviderService serviceProviderService,
                               ChromeDriverManager chromeDriverManager,
                               YandexUslugiParser yandexUslugiParser,
                               @Value("${threads.number}") int numberOfThreads, Downloader downloader) {
        this.categoryService = categoryService;
        this.categoryAttributeService = categoryAttributeService;
        this.serviceProviderService = serviceProviderService;
        this.chromeDriverManager = chromeDriverManager;
        this.yandexUslugiParser = yandexUslugiParser;
        this.executorService = Executors.newFixedThreadPool(numberOfThreads);
        this.downloader = downloader;
    }

    @SneakyThrows
    public void scrape() {
        Set<Map.Entry<String, String>> regionEntries = RegionProvider.regionMap.entrySet();
//        List<Category> levelTwoCategories = scrapeTwoLevelsOfCategories(regionEntries.iterator().next().getValue());
//        levelTwoCategories.parallelStream().forEach(categoryService::save);
        for (Map.Entry<String, String> entry : regionEntries) {
            scrapeRegion(entry);
        }
    }

    @SneakyThrows
    private void scrapeRegion(Map.Entry<String, String> regionEntry) {
        logger.info("started region {}", regionEntry.getKey());
        List<Category> allLevelTwoCategories = categoryService.getAllLevelTwoCategories();
        List<Future> futures = new ArrayList<>();
        if (regionEntry.getValue().equals("10853-vologda-oblast")) {
            allLevelTwoCategories = allLevelTwoCategories.subList(35, allLevelTwoCategories.size());
        }
        for (Category category : allLevelTwoCategories) {
            futures.add(executorService.submit(() -> {
                List<Category> categories = scrapeCategoryForSubcategories(category, regionEntry.getValue(), false);
                for (Category subcategory : categories) {
                    scrapeAndSaveAllServiceProviderUrlsInCategory(subcategory, regionEntry.getValue(), true);
                    scrapeAndSaveAllServiceProviderUrlsInCategory(subcategory, regionEntry.getValue(), false);
                }
            }));
        }
        for (Future future : futures) {
            future.get();
        }
    }

    @SneakyThrows
    private List<Category> scrapeCategoryForSubcategories(Category category, String regionUrl, boolean isLevel3) {
        logger.info("started category {} | {} | {}", category.getCategoryName(), category.getSubcategory1Name(), category.getSubcategory2Name());
        List<Category> level3Categories = new ArrayList<>();
        String url = String.format("https://yandex.ru/uslugi/%s/category%s", regionUrl, category.getUrl());
        String pageSource = downloader.getWithCookies(url);
        List<CategoryAttribute> categoryAttributes = scrapeCategoryAttributes(category, pageSource);
        categoryAttributes.parallelStream().forEach(categoryAttribute -> categoryAttributeService.save(categoryAttribute, category));
        if (!isLevel3) {
            level3Categories = scrapeLevel3Categories(category, pageSource);
        }
        List<Category> savedLevel3Categories = new ArrayList<>();
        for (Category level3Category : level3Categories) {
            savedLevel3Categories.add(categoryService.save(level3Category));
        }
        level3Categories = savedLevel3Categories;

        for (Category level3Category : level3Categories) {
            scrapeCategoryForSubcategories(level3Category, regionUrl, true);
        }

        return level3Categories;
    }

    @SneakyThrows
    private List<Category> scrapeLevel3Categories(Category level2Category, String pageSource) {
        List<Category> level3Categories = new ArrayList<>();
        Document document = Jsoup.parse(pageSource);
        Element scriptEl = document.selectFirst("script[nonce]");
        String scriptSource = StringUtils.substringBetween(scriptEl.toString(), ".__PRELOADED_STATE__=", ";window.__CSRF_TOKEN__=");
        JSONParser jsonParser = new JSONParser();
        JSONObject mainJsonObject = (JSONObject) jsonParser.parse(scriptSource);
        JSONObject searchObject = (JSONObject) mainJsonObject.get("search");
        JSONObject paramObject = (JSONObject) searchObject.get("params");
        JSONArray filtersArray = (JSONArray) paramObject.get("filters");
        JSONObject firstFilterObject = (JSONObject) filtersArray.get(0);
        JSONArray occupationsArray = (JSONArray) firstFilterObject.get("occupations");
        JSONObject firstOccupationObject = (JSONObject) occupationsArray.get(0);
        JSONArray specializationsArray = (JSONArray) firstOccupationObject.get("specializations");
        JSONObject firstSpecializationObject = (JSONObject) specializationsArray.get(0);
        JSONArray servicesArray = (JSONArray) firstSpecializationObject.get("services");

        for (Object o : servicesArray) {
            JSONObject serviceObject = (JSONObject) o;

            String subcategory2Name = (String) serviceObject.get("name");
            String serviceUrl = (String) serviceObject.get("seoId");
            String serviceId = (String) serviceObject.get("id");
            String categoryUrl = serviceUrl + "--" + serviceId;

            Category category = new Category();
            category.setCategoryName(level2Category.getCategoryName());
            category.setSubcategory1Name(level2Category.getSubcategory1Name());
            category.setSubcategory2Name(subcategory2Name);
            category.setUrl(categoryUrl);
            level3Categories.add(category);
        }
        return level3Categories;
    }

    private List<CategoryAttribute> scrapeCategoryAttributes(Category category, String pageSource) {
        List<CategoryAttribute> categoryAttributes = new ArrayList<>();
        Document document = Jsoup.parse(pageSource);
        Elements filterElements = document.select("div.Filters-ItemContainer");
        int startIndex = category.getSubcategory2Name() == null ? 1 : 0;
        for (int i = startIndex; i < filterElements.size(); i++) {
            Element filterElement = filterElements.get(i);
            Elements options = filterElement.select("div.Checkbox-Text");
            if (filterElement.selectFirst("h3:contains(Место)") != null) {
                continue;
            }
            List<String> names = new ArrayList<>();
            List<String> values = new ArrayList<>();

            Element titleEl = filterElement.selectFirst("div.Title,h3.Title");
            if (titleEl != null) {
                names.add(titleEl.text());
                for (Element option : options) {
                    values.add(option.text());
                }
            } else {
                for (Element option : options) {
                    names.add(option.text());
                }
                values.add("true");
                values.add("false");
            }

            for (String name : names) {
                for (String value : values) {
                    CategoryAttribute categoryAttribute = new CategoryAttribute();
                    categoryAttribute.setCategory(category);
                    categoryAttribute.setName(name);
                    categoryAttribute.setValue(value);
                    categoryAttributes.add(categoryAttribute);
                }
            }
        }
        return categoryAttributes;
    }

    @SneakyThrows
    private List<Category> scrapeTwoLevelsOfCategories(String regionUrl) {
        List<Category> categories = new ArrayList<>();
        String url = String.format("https://yandex.ru/uslugi/%s/catalog", regionUrl);
        String source = downloader.getWithCookies(url);
        Document document = Jsoup.parse(source);
        Element scriptEl = document.selectFirst("script[nonce]");
        String scriptSource = StringUtils.substringBetween(scriptEl.toString(), ".__PRELOADED_STATE__=", ";window.__CSRF_TOKEN__=");
        JSONParser jsonParser = new JSONParser();
        JSONObject mainJsonObject = (JSONObject) jsonParser.parse(scriptSource);
        JSONObject rubricsObject = (JSONObject) mainJsonObject.get("rubrics");
        categories.addAll(getCategoriesFromJsonObject("occupations", rubricsObject));
        categories.addAll(getCategoriesFromJsonObject("extraOccupations", rubricsObject));
        return categories;
    }

    @SneakyThrows
    private void scrapeAndSaveAllServiceProviderUrlsInCategory(Category category, String regionUrl, Boolean isCompany) {
        logger.info("Scraping service providers in category {} | {} | {}...", category.getCategoryName(), category.getSubcategory1Name(), category.getSubcategory2Name());
        String appender = isCompany ? ORGANIZATION_WORKER_URL_APPENDER : PRIVATE_WORKER_URL_APPENDER;
        String url = String.format("https://yandex.ru/uslugi/%s/category%s%s", regionUrl, category.getUrl(), appender);
        String source;
        Document document;
        int counter = 0;
        Element nextPageEl;
        source = downloader.getWithCookies(url);
        document = Jsoup.parse(source);
        do {
            nextPageEl = document.selectFirst("span:contains(Далее)");
            Elements urlElems = document.select("div.WorkersListBlendered-WorkerCard a.WorkerCardMini-Title");
            for (Element urlElem : urlElems) {
                String serviceProviderUrl = "https://yandex.ru" + StringUtils.substringBefore(urlElem.attr("href"), "?");
                ServiceProvider serviceProvider = new ServiceProvider();
                serviceProvider.setUrl(serviceProviderUrl);
                serviceProvider.setIsCompany(isCompany);
                serviceProvider.setParsed(false);
                serviceProvider.getCategories().add(category);
                serviceProviderService.save(serviceProvider);
            }
            if (nextPageEl != null) {
                url = String.format("https://yandex.ru/uslugi/%s/category%s%s", regionUrl, category.getUrl(), appender) + "&p=" + ++counter;
                source = downloader.getWithCookies(url);
                document = Jsoup.parse(source);
            }
        } while (nextPageEl != null);
    }

    private List<Category> getCategoriesFromJsonObject(String objectName, JSONObject mainJsonObject) {
        List<Category> categories = new ArrayList<>();
        JSONArray rubricsArray = (JSONArray) mainJsonObject.get(objectName);
        for (Object o : rubricsArray) {
            JSONObject categoryObject = (JSONObject) o;
            String categoryName = (String) categoryObject.get("name");
            JSONArray subcategoryArray = (JSONArray) categoryObject.get("specializations");

            for (Object o1 : subcategoryArray) {
                JSONObject subcategory1Object = (JSONObject) o1;
                Long id = (Long) subcategory1Object.get("numberId");
                String subcategory1Name = (String) subcategory1Object.get("name");
                String seoId = (String) subcategory1Object.get("seoId");
                String categoryUrl = seoId.replaceAll("\\u002F", "/") + "--" + id;

                Category category = new Category();
                category.setCategoryName(categoryName);
                category.setSubcategory1Name(subcategory1Name);
                category.setUrl(categoryUrl);
                categories.add(category);
            }
        }
        return categories;
    }

    @Override
    public void run(String... args) throws Exception {
        chromeDriverManager.getPageSource("https://yandex.ru/uslugi/1-moscow-and-moscow-oblast/category/hozyajstvo-i-uborka/uborka--3124");
        scrape();
    }
}
