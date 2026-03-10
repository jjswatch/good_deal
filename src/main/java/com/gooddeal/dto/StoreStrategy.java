package com.gooddeal.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StoreStrategy {

    private String storeName;
    private BigDecimal total;
    private int coveredCount;   // 能買到幾項
    private int totalCount;     // 清單總項目
    private List<SplitItem> items;
    private boolean preferred;

    public String getCoverageText() {
        return coveredCount + " / " + totalCount + " 項";
    }
}
