package com.saraiev.yandexuslugiscraper.service;

import com.saraiev.yandexuslugiscraper.domain.ServiceProvider;
import com.saraiev.yandexuslugiscraper.repository.ServiceProviderRepository;
import org.springframework.stereotype.Service;

@Service
public class ServiceProviderService {

    private final ServiceProviderRepository serviceProviderRepository;

    public ServiceProviderService(ServiceProviderRepository serviceProviderRepository) {
        this.serviceProviderRepository = serviceProviderRepository;
    }

    public ServiceProvider get(String url) {
        return serviceProviderRepository.findByUrl(url);
    }

    public ServiceProvider save(ServiceProvider serviceProvider) {
        ServiceProvider serviceProviderByUrl = serviceProviderRepository.findByUrl(serviceProvider.getUrl());
        if(serviceProviderByUrl != null) {
            return serviceProviderByUrl;
        } else {
            return serviceProviderRepository.save(serviceProvider);
        }
    }

}
