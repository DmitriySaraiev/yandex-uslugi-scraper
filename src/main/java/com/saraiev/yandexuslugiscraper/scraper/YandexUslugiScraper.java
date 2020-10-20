package com.saraiev.yandexuslugiscraper.scraper;

import com.saraiev.yandexuslugiscraper.domain.Category;
import com.saraiev.yandexuslugiscraper.domain.CategoryAttribute;
import com.saraiev.yandexuslugiscraper.domain.ChromeDriverWrapper;
import com.saraiev.yandexuslugiscraper.service.CategoryAttributeService;
import com.saraiev.yandexuslugiscraper.service.CategoryService;
import com.saraiev.yandexuslugiscraper.service.ServiceProviderService;
import com.saraiev.yandexuslugiscraper.utils.ChromeDriverManager;
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
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
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

@Component
public class YandexUslugiScraper implements CommandLineRunner {

    private final static Logger logger = LoggerFactory.getLogger(YandexUslugiScraper.class);

    private final CategoryService categoryService;
    private final CategoryAttributeService categoryAttributeService;
    private final ServiceProviderService serviceProviderService;
    private final ChromeDriverManager chromeDriverManager;
    private final YandexUslugiParser yandexUslugiParser;
    private final ExecutorService executorService;

    public YandexUslugiScraper(CategoryService categoryService,
                               CategoryAttributeService categoryAttributeService,
                               ServiceProviderService serviceProviderService,
                               ChromeDriverManager chromeDriverManager,
                               YandexUslugiParser yandexUslugiParser,
                               @Value("${threads.number}") int numberOfThreads) {
        this.categoryService = categoryService;
        this.categoryAttributeService = categoryAttributeService;
        this.serviceProviderService = serviceProviderService;
        this.chromeDriverManager = chromeDriverManager;
        this.yandexUslugiParser = yandexUslugiParser;
        this.executorService = Executors.newFixedThreadPool(numberOfThreads);
    }

    public void scrape() {
        Set<Map.Entry<String, String>> regionEntries = RegionProvider.regionMap.entrySet();
        List<Category> levelTwoCategories = scrapeTwoLevelsOfCategoriesInRegion(regionEntries.iterator().next().getValue());
        levelTwoCategories.parallelStream().forEach(categoryService::save);
        for (Map.Entry<String, String> entry : regionEntries) {
            scrapeRegion(entry);
        }
    }

    private void scrapeRegion(Map.Entry<String, String> regionEntry) {
        logger.info("started region {}", regionEntry.getKey());
        List<Category> allLevelTwoCategories = categoryService.getAllLevelTwoCategories();
        for (Category category : allLevelTwoCategories) {
            scrapeCategory(category, regionEntry.getValue());
        }
    }

    private void scrapeCategory(Category category, String regionUrl) {
        logger.info("started category lvl 2 {} {}", category.getCategoryName(), category.getSubcategory1Name());
        ChromeDriverWrapper chromeDriverWrapper = chromeDriverManager.getFreeChromeDriver();
        chromeDriverWrapper.setBusy(true);
        ChromeDriver chromeDriver = chromeDriverWrapper.getChromeDriver();
        String url = String.format("https://yandex.ru/uslugi/%s/category%s", regionUrl, category.getUrl());
        chromeDriverManager.loadPage(chromeDriverWrapper, url);
        try {
            WebElement showAllCategoriesElem = chromeDriver.findElement(By.cssSelector("div.Filters span.YdoIcon"));
            showAllCategoriesElem.click();
        } catch (NoSuchElementException ignored) {
        }
        String pageSource = chromeDriverManager.getPageSource(chromeDriverWrapper);
        List<CategoryAttribute> categoryAttributes = scrapeCategoryAttributes(category, pageSource);
        categoryAttributes.parallelStream().forEach(categoryAttribute -> categoryAttributeService.save(categoryAttribute, category));
        List<Category> level3Categories = scrapeLevel3Categories(category, pageSource);
        level3Categories.parallelStream().forEach(categoryService::save);
    }

    private List<Category> scrapeLevel3Categories(Category level2Category, String pageSource) {
        List<Category> level3Categories = new ArrayList<>();
        Document document = Jsoup.parse(pageSource);
        Elements subcategory2Elems = document.select("div.Filters-RubricsList a");
        for (Element subcategory2Elem : subcategory2Elems) {
            String subcategory2Name = subcategory2Elem.text();
            if (subcategory2Name.equals("Свернуть")) {
                continue;
            }
            String categoryUrl = StringUtils.substringAfterLast(subcategory2Elem.attr("href"), "category/");
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
    private List<Category> scrapeTwoLevelsOfCategoriesInRegion(String regionUrl) {
        List<Category> categories = new ArrayList<>();
        String url = String.format("https://yandex.ru/uslugi/%s/catalog", regionUrl);
        ChromeDriverWrapper chromeDriverWrapper = chromeDriverManager.getFreeChromeDriver();
        String source = chromeDriverManager.getPageSource(chromeDriverWrapper, url);
        Document document = Jsoup.parse(source);
        Element scriptEl = document.selectFirst("script[nonce]");
        String scriptSource = StringUtils.substringBetween(scriptEl.toString(), ".__PRELOADED_STATE__=", ";window.__CSRF_TOKEN__=");
        JSONParser jsonParser = new JSONParser();
        JSONObject mainJsonObject = (JSONObject) jsonParser.parse(scriptSource);
        JSONObject rubricsObject = (JSONObject) mainJsonObject.get("rubrics");
        categories.addAll(getCategoriesFromJsonObject("occupations", rubricsObject));
        categories.addAll(getCategoriesFromJsonObject("extraOccupations", rubricsObject));
        chromeDriverWrapper.setBusy(false);
        return categories;
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
    public void run(String... args) {
//        Category category = categoryService.get(7L);
//        scrapeCategory(category, "213-moscow");
        scrape();
    }
}
