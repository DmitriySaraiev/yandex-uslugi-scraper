package com.saraiev.yandexuslugiscraper.domain;

import lombok.Data;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Data
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

    @ManyToMany
    @JoinTable(
            name = "service_provider_category",
            joinColumns = @JoinColumn(name = "category_id"),
            inverseJoinColumns = @JoinColumn(name = "service_provider_id"))
    private Set<ServiceProvider> serviceProviders = new HashSet<>();;

    @OneToMany(mappedBy="category")
    private Set<CategoryAttribute> attributes = new HashSet<>();;

}
