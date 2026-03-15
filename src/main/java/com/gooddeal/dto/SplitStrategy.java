package com.gooddeal.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SplitStrategy {

    private BigDecimal total;
    private List<SplitItem> items;
    private int coveredCount;      // 分開買總共買到幾件
    private int totalCount;        // 應該要買幾件
    private int uniqueStoreCount;  // 最終要去幾家不同的店
}
