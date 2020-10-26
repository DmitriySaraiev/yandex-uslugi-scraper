package com.saraiev.yandexuslugiscraper.service;

import com.saraiev.yandexuslugiscraper.domain.ServiceProvider;
import com.saraiev.yandexuslugiscraper.repository.ServiceProviderRepository;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ServiceProviderService {

    private final static Logger logger = LoggerFactory.getLogger(ServiceProviderService.class);

    private final ServiceProviderRepository serviceProviderRepository;

    public ServiceProviderService(ServiceProviderRepository serviceProviderRepository) {
        this.serviceProviderRepository = serviceProviderRepository;
    }

    public ServiceProvider get(String url) {
        return serviceProviderRepository.findByUrl(url);
    }
    public List<ServiceProvider> getAllParsed(Boolean parsed) {
        return serviceProviderRepository.findAllByParsed(parsed);
    }

    public ServiceProvider save(ServiceProvider serviceProvider) {
        if(serviceProvider.getId() != null) {
            return serviceProviderRepository.save(serviceProvider);
        }
        ServiceProvider serviceProviderByUrl = serviceProviderRepository.findByUrl(serviceProvider.getUrl());
        if (serviceProviderByUrl != null) {
            logger.info("Service provider {} {} already exists", serviceProvider.getUrl(), serviceProvider.getName());
            if(!serviceProviderByUrl.getCategories().equals(serviceProvider.getCategories())) {
                serviceProviderByUrl.getCategories().addAll(serviceProvider.getCategories());
                serviceProviderRepository.save(serviceProviderByUrl);
            }
            return serviceProviderByUrl;
        } else {
            logger.info("Saved service provider {}", serviceProvider.getUrl());
            return serviceProviderRepository.save(serviceProvider);
        }
    }

}
