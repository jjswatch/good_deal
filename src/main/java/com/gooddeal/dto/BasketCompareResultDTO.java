package com.gooddeal.dto;

import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class BasketCompareResultDTO {

    private Integer splitTotal; // 拆單最低總價
    private List<BasketPriceDTO> splitItems;

    private String bestStoreName; // 一站式最佳店家
    private Integer bestStoreTotal;

    // 每間店總價（storeName -> total）
    private Map<String, Integer> storeTotals;
}
