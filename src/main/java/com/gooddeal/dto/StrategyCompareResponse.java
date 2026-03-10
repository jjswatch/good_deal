package com.gooddeal.dto;

import lombok.Data;

@Data
public class StrategyCompareResponse {

    private SplitStrategy splitStrategy;

    private StoreStrategy bestStore;

    private StrategyType strategyType;

    private String message;

}