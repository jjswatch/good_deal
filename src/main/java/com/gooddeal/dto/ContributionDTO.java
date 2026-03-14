package com.gooddeal.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ContributionDTO {
    private Integer reportId;
    private String productName;
    private String brand;
    private String storeName;
    private BigDecimal reportedPrice;      // 對應前端 item.price
    private String status;         // APPROVED, PENDING, REJECTED
    private LocalDateTime reportedAt; // 對應前端 item.createdAt
}