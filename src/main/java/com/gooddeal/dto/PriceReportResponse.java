package com.gooddeal.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PriceReportResponse {
    private Integer reportId;
    private String storeName;
    private BigDecimal price;
    private String userName;
    private LocalDateTime date;
}
