package com.gooddeal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BasketPriceDTO {
    private Integer productId;
    private String productName;
    private Integer storeId;
    private String storeName;
    private Integer price;
}
