package com.gooddeal.strategy;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.gooddeal.dto.StoreStrategy;
import com.gooddeal.dto.SplitItem;
import com.gooddeal.model.ProductPrices;

@Service
public class PreferredStoreStrategy {
	
	private static final BigDecimal TOLERANCE = BigDecimal.valueOf(30);

    public StoreStrategy calculate(
            List<ProductPrices> prices,
            List<Integer> productIds,
            List<Integer> preferredStores
    ) {

        return prices.stream()
                .filter(p -> preferredStores.contains(
                        p.getStore().getStoreId()))
                .collect(Collectors.groupingBy(ProductPrices::getStore))
                .entrySet()
                .stream()
                .map(e -> {

                    BigDecimal total = e.getValue().stream()
                            .map(ProductPrices::getPrice)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    return new StoreStrategy(
                            e.getKey().getStoreName(),
                            total,
                            e.getValue().size(),
                            productIds.size(),
                            e.getValue().stream()
                            .map((ProductPrices p) -> new SplitItem(p)) // 強制宣告 p 的型別
                            .collect(Collectors.toList()),
                            true
                    );

                })
                .filter(s -> (double)s.getCoveredCount() / productIds.size() >= 0.5) 
                .min(Comparator.comparing(StoreStrategy::getCoveredCount).reversed()
                      .thenComparing(StoreStrategy::getTotal))
                .orElse(null);
    }
    
    public StoreStrategy recommend(
            StoreStrategy preferred,
            BigDecimal cheapestTotal,
            Integer userId
    ) {

        if (preferred == null) return null;

        BigDecimal diff =
                preferred.getTotal().subtract(cheapestTotal);

        if (diff.compareTo(TOLERANCE) <= 0) {
            preferred.setPreferred(true);
            return preferred;
        }

        return null;
    }
}
