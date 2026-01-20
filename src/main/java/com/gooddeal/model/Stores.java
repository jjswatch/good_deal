package com.gooddeal.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "stores")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Stores {

    @Id
    @Column(name = "store_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer storeId;

    @Column(name = "store_name", nullable = false, unique = true, length = 100)
    private String storeName;

    private String location;
    private String website;
    
    @Column(name = "store_group")
    private Integer storeGroup;
}
