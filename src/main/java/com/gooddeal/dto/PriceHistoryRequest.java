package com.gooddeal.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class PriceHistoryRequest {
    private Integer productId;
    private Integer storeId;
    private BigDecimal oldPrice;
    private BigDecimal newPrice;
}
