package com.gooddeal.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class PriceHistoryResponse {
    private Integer historyId;
    private Integer productId;
    private String brand;
    private String productName; // 方便前端顯示
    private String productImage;
    private Integer storeId;
    private String storeName;   // 方便前端顯示
    private BigDecimal oldPrice;
    private BigDecimal newPrice;
    private LocalDateTime changedAt;
}
