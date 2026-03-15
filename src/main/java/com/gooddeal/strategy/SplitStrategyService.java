package com.gooddeal.strategy;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.gooddeal.dto.SplitItem;
import com.gooddeal.dto.SplitStrategy;
import com.gooddeal.model.ProductPrices;

@Service
public class SplitStrategyService {

    public SplitStrategy calculate(List<ProductPrices> prices, List<Integer> targetProductIds) {
        // 1. 商品分組
        Map<Integer, List<ProductPrices>> byProduct = prices.stream()
                .collect(Collectors.groupingBy(p -> p.getProduct().getProductId()));

        // 2. 找出各品項最低價
        List<SplitItem> items = byProduct.values().stream()
                .map(list -> list.stream()
                        .min(Comparator.comparing(ProductPrices::getPrice))
                        .orElseThrow())
                // 👇 關鍵修正：明確指定 (ProductPrices p)
                .map((ProductPrices p) -> new SplitItem(
                        p.getProduct().getProductId(),
                        p.getProduct().getBrand(),
                        p.getProduct().getProductName(),
                        p.getStore().getStoreName(),
                        p.getPrice()
                ))
                .toList();

        // 3. 計算總價與店家數
        BigDecimal total = items.stream()
                .map(SplitItem::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int uniqueStoreCount = (int) items.stream()
                .map(SplitItem::getStoreName)
                .distinct()
                .count();

        return new SplitStrategy(
            total, 
            items, 
            items.size(), 
            targetProductIds.size(), 
            uniqueStoreCount
        );
    }
}