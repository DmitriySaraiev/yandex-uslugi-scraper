package com.saraiev.yandexuslugiscraper.service;

import com.saraiev.yandexuslugiscraper.domain.Category;
import com.saraiev.yandexuslugiscraper.domain.CategoryAttribute;
import com.saraiev.yandexuslugiscraper.repository.CategoryAttributeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CategoryAttributeService {

    private final static Logger logger = LoggerFactory.getLogger(CategoryAttributeService.class);

    private final CategoryAttributeRepository categoryAttributeRepository;

    public CategoryAttributeService(CategoryAttributeRepository categoryAttributeRepository) {
        this.categoryAttributeRepository = categoryAttributeRepository;
    }

    public CategoryAttribute save(CategoryAttribute categoryAttribute, Category category) {
        CategoryAttribute existingCategoryAttribute = categoryAttributeRepository.findByCategoryAndNameAndValue(category, categoryAttribute.getName(), categoryAttribute.getValue());
        if (existingCategoryAttribute != null) {
//            logger.info("attribute {}::{} for category {} | {} | {} already exists",
//                    categoryAttribute.getName(), categoryAttribute.getValue(),
//                    category.getCategoryName(), category.getSubcategory1Name(), category.getSubcategory2Name());
            return existingCategoryAttribute;
        }
//        logger.info("saved attribute {}::{} for category {} | {} | {}",
//                categoryAttribute.getName(), categoryAttribute.getValue(),
//                category.getCategoryName(), category.getSubcategory1Name(), category.getSubcategory2Name());
        return categoryAttributeRepository.save(categoryAttribute);
    }

}
