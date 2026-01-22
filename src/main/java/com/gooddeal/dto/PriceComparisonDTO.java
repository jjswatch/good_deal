package com.gooddeal.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PriceComparisonDTO {

    private Integer productId;
    private String productName;
    private String storeName;
    private BigDecimal price;
}
