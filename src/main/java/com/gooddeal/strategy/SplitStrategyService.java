package com.gooddeal.strategy;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.gooddeal.dto.SplitItem;
import com.gooddeal.dto.SplitStrategy;
import com.gooddeal.model.ProductPrices;

@Service
public class SplitStrategyService {

    public SplitStrategy calculate(List<ProductPrices> prices) {

        // 商品分組
        Map<Integer, List<ProductPrices>> byProduct =
                prices.stream()
                        .collect(Collectors.groupingBy(
                                p -> p.getProduct().getProductId()
                        ));

        List<SplitItem> items =
                byProduct.values().stream()
                        .map(list -> list.stream()
                                .min((a,b) ->
                                        a.getPrice().compareTo(b.getPrice()))
                                .orElseThrow())
                        .map(p -> new SplitItem(
                                p.getProduct().getProductId(),
                                p.getProduct().getBrand(),
                                p.getProduct().getProductName(),
                                p.getStore().getStoreName(),
                                p.getPrice()
                        ))
                        .toList();

        BigDecimal total =
                items.stream()
                        .map(SplitItem::getPrice)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new SplitStrategy(total, items);
    }
}