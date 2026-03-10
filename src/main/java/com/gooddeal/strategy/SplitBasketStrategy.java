package com.gooddeal.strategy;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.gooddeal.dto.SplitItem;
import com.gooddeal.dto.SplitStrategy;
import com.gooddeal.model.ProductPrices;

@Service
public class SplitBasketStrategy {

    public SplitStrategy calculate(
            List<ProductPrices> prices,
            List<Integer> preferredStores
    ) {

        Map<Integer, ProductPrices> best = prices.stream()
                .collect(Collectors.groupingBy(
                        p -> p.getProduct().getProductId(),
                        Collectors.collectingAndThen(
                                Collectors.minBy((a, b) -> {

                                    boolean aPref = preferredStores.contains(
                                            a.getStore().getStoreId());

                                    boolean bPref = preferredStores.contains(
                                            b.getStore().getStoreId());

                                    if (aPref && !bPref) return -1;
                                    if (!aPref && bPref) return 1;

                                    return a.getPrice().compareTo(b.getPrice());

                                }),
                                Optional::get
                        )
                ));

        List<SplitItem> items = best.values().stream()
                .map(p -> new SplitItem(
                        p.getProduct().getProductId(),
                        p.getProduct().getBrand(),
                        p.getProduct().getProductName(),
                        p.getStore().getStoreName(),
                        p.getPrice()
                ))
                .toList();

        BigDecimal total = items.stream()
                .map(SplitItem::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new SplitStrategy(total, items);
    }
}
