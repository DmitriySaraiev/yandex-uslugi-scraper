package com.saraiev.yandexuslugiscraper.repository;

import com.saraiev.yandexuslugiscraper.domain.Category;
import org.springframework.data.repository.CrudRepository;

public interface CategoryRepository extends CrudRepository <Category, Long> {
}
