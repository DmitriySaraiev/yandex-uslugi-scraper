package com.saraiev.yandexuslugiscraper.service;

import com.saraiev.yandexuslugiscraper.domain.Category;
import com.saraiev.yandexuslugiscraper.repository.CategoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {

    private final static Logger logger = LoggerFactory.getLogger(CategoryService.class);

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public Category get(Long id) {
        Optional<Category> optionalCategory = categoryRepository.findById(id);
        return optionalCategory.orElse(null);
    }

    public Category save(Category category) {
        Category existingCategory;
        if (category.getSubcategory2Name() == null) {
            existingCategory = categoryRepository.findByCategoryNameAndSubcategory1Name(category.getCategoryName(), category.getSubcategory1Name());
        } else {
            existingCategory = categoryRepository.findByCategoryNameAndSubcategory1NameAndSubcategory2Name(category.getCategoryName(), category.getSubcategory1Name(), category.getSubcategory2Name());
        }
        if (existingCategory != null) {
            logger.info("category {} | {} already exists", category.getCategoryName(), category.getSubcategory1Name());
            return existingCategory;
        }
        logger.info("creating category {} | {} | {}", category.getCategoryName(), category.getSubcategory1Name(), category.getSubcategory2Name() == null ? "" : category.getSubcategory2Name());
        return categoryRepository.save(category);
    }

    public List<Category> getAllLevelTwoCategories() {
        return categoryRepository.findAllBySubcategory2NameIsNull();
    }
}
