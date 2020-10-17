package com.saraiev.yandexuslugiscraper.service;

import com.saraiev.yandexuslugiscraper.repository.CategoryAttributeRepository;
import org.springframework.stereotype.Service;

@Service
public class CategoryAttributeService {

    private final CategoryAttributeRepository categoryAttributeRepository;

    public CategoryAttributeService(CategoryAttributeRepository categoryAttributeRepository) {
        this.categoryAttributeRepository = categoryAttributeRepository;
    }
}
