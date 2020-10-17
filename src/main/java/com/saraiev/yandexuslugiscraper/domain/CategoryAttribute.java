package com.saraiev.yandexuslugiscraper.domain;

import lombok.Data;

import javax.persistence.*;
import java.util.Set;

@Data
@Entity
@Table(name = "category_atribute")
public class CategoryAttribute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String type;
    private String value;

    @ManyToOne
    @JoinColumn(name="categoty_id", nullable=false)
    private Category category;

}
