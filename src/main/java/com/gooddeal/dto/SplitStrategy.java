package com.gooddeal.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SplitStrategy {

    private BigDecimal total;
    private List<SplitItem> items;
}
