create table service_provider
(
    id                   bigint auto_increment
        primary key,
    name                 varchar(200)  null,
    is_company           tinyint(1)    null,
    is_passport_verified tinyint(1)    null,
    location             varchar(1000) null,
    social_networks      varchar(100)  null,
    rating               float         null
);

create table category
(
    id                bigint auto_increment
        primary key,
    category_name     varchar(200) null,
    subcategory1_name varchar(200) null,
    subcategory2_name varchar(200) null,
    constraint category_name
        unique (category_name, subcategory1_name, subcategory2_name)
);

create table category_atribute
(
    id          bigint auto_increment
        primary key,
    category_id bigint       not null,
    type        varchar(50)  not null,
    name        varchar(100) not null,
    value       varchar(100) not null,
    constraint category_atribute_category_id_fk
        foreign key (category_id) references category (id)
);

create table service_provider_category
(
    id                  bigint auto_increment
        primary key,
    service_provider_id bigint not null,
    category_id         bigint not null,
    constraint service_provider_category_category_id_fk
        foreign key (category_id) references category (id),
    constraint service_provider_category_service_provider_id_fk
        foreign key (service_provider_id) references service_provider (id)
);