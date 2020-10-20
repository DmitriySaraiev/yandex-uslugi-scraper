package com.saraiev.yandexuslugiscraper.repository;

import com.saraiev.yandexuslugiscraper.domain.Category;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface CategoryRepository extends CrudRepository<Category, Long> {

    Category findByCategoryNameAndSubcategory1Name(String categoryName, String subcategory1Name);

    Category findByCategoryNameAndSubcategory1NameAndSubcategory2Name(String categoryName, String subcategory1Name, String subcategory2Name);

    List<Category> findAllBySubcategory2NameIsNull();

}
