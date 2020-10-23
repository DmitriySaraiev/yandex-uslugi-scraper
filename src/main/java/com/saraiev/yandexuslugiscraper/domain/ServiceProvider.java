package com.saraiev.yandexuslugiscraper.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
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
    private Float rating;
    private String phone;
    private String vk;
    private String facebook;
    private String youtube;
    private String instagram;
    private String profiru;
    private String telegram;
    private String whatsapp;
    private String viber;
    private String email;
    private Boolean parsed;

    @Column(unique = true)
    private String url;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "service_provider_category",
            joinColumns = @JoinColumn(name = "service_provider_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id"))
    private Set<Category> categories = new HashSet<>();

}
