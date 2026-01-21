package com.gooddeal.dto;

import lombok.Data;

@Data
public class ProductRequest {
    private String productName;
    private String brand;
    private String spec;
    private String imageUrl;
    private String barcode;
    private Integer categoryId;
}
