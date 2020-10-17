package com.saraiev.yandexuslugiscraper.domain;

import lombok.Data;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Table(name = "service_provider")
public class ServiceProvider {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private Boolean isCompany;
    private Boolean isPassportVerified;
    private String location;
    private String socialNetworks;
    private Float rating;

    @Column(unique=true)
    private String url;

    @ManyToMany
    @JoinTable(
            name = "service_provider_category",
            joinColumns = @JoinColumn(name = "service_provider_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id"))
    private Set<Category> categories = new HashSet<>();

}
