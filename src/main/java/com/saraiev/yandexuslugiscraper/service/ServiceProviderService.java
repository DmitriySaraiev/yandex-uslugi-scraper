package com.saraiev.yandexuslugiscraper.service;

import com.saraiev.yandexuslugiscraper.repository.ServiceProviderRepository;
import org.springframework.stereotype.Service;

@Service
public class ServiceProviderService {

    private final ServiceProviderRepository serviceProviderRepository;

    public ServiceProviderService(ServiceProviderRepository serviceProviderRepository) {
        this.serviceProviderRepository = serviceProviderRepository;
    }

}
