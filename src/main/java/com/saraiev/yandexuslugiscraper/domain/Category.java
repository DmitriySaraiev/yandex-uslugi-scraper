package com.saraiev.yandexuslugiscraper.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "category")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String categoryName;
    @Column(name = "subcategory1_name")
    private String subcategory1Name;
    @Column(name = "subcategory2_name")
    private String subcategory2Name;
    private String url;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "service_provider_category",
            joinColumns = @JoinColumn(name = "category_id"),
            inverseJoinColumns = @JoinColumn(name = "service_provider_id"))
    private Set<ServiceProvider> serviceProviders = new HashSet<>();;

    @OneToMany(mappedBy="category", fetch = FetchType.LAZY)
    private Set<CategoryAttribute> attributes = new HashSet<>();;

}
