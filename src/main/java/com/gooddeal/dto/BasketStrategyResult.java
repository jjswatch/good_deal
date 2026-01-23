package com.gooddeal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BasketStrategyResult {

	private StrategyType recommend;   // ⭐ 前端高亮用
    private String recommendation;    // ⭐ 顯示文字
    private SplitStrategy splitStrategy;
    private StoreStrategy bestStore;
}
