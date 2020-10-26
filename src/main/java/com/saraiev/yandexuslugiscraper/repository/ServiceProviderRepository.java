package com.saraiev.yandexuslugiscraper.repository;

import com.saraiev.yandexuslugiscraper.domain.ServiceProvider;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ServiceProviderRepository extends CrudRepository<ServiceProvider, Long> {

    ServiceProvider findByUrl(String url);

    List<ServiceProvider> findAllByParsed(Boolean parsed);

}
