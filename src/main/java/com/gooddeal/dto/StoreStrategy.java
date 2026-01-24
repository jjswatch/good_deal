package com.gooddeal.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StoreStrategy {

    private String storeName;
    private BigDecimal total;
    private int coveredCount;   // 能買到幾項
    private int totalCount;     // 清單總項目
    private List<String> missingItems;

    public String getCoverageText() {
        return coveredCount + " / " + totalCount + " 項";
    }
}
