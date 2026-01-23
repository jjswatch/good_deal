package com.gooddeal.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SplitItem {

    private Integer productId;
    private String storeName;
    private BigDecimal price;
}
