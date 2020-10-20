package com.saraiev.yandexuslugiscraper.repository;

import com.saraiev.yandexuslugiscraper.domain.Category;
import com.saraiev.yandexuslugiscraper.domain.CategoryAttribute;
import org.springframework.data.repository.CrudRepository;

public interface CategoryAttributeRepository extends CrudRepository <CategoryAttribute, Long> {

    CategoryAttribute findByCategoryAndNameAndValue(Category category, String name, String value);

}
