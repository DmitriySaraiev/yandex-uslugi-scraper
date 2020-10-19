package com.saraiev.yandexuslugiscraper.service;

import com.saraiev.yandexuslugiscraper.domain.Category;
import com.saraiev.yandexuslugiscraper.repository.CategoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    private final static Logger logger = LoggerFactory.getLogger(CategoryService.class);

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public Category save(Category category) {
        Category existingCategory = categoryRepository.findByCategoryNameAndSubcategory1Name(category.getCategoryName(), category.getSubcategory1Name());
        if (existingCategory != null) {
            logger.info("category {} {} already exists", category.getCategoryName(), category.getSubcategory1Name());
            return existingCategory;
        }
        logger.info("creating category {} {}", category.getCategoryName(), category.getSubcategory1Name());
        return categoryRepository.save(category);
    }

    public List<Category> getAllLevelTwoCategories() {
        return categoryRepository.findAllBySubcategory2NameIsNull();
    }
}
