package com.gooddeal.strategy;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.gooddeal.dto.SplitItem;
import com.gooddeal.dto.StoreStrategy;
import com.gooddeal.model.ProductPrices;
import com.gooddeal.model.Stores;

@Service
public class OneStoreStrategy {

    public StoreStrategy calculate(
            List<ProductPrices> prices,
            List<Integer> productIds
    ) {

        Map<Stores, List<ProductPrices>> byStore =
                prices.stream().collect(Collectors.groupingBy(
                        ProductPrices::getStore
                ));

        StoreStrategy best = null;

        for (var entry : byStore.entrySet()) {

            Stores store = entry.getKey();
            List<ProductPrices> list = entry.getValue();

            BigDecimal total = list.stream()
                    .map(ProductPrices::getPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            int coverage = list.size();

            List<SplitItem> items = list.stream()
            	    .map(p -> new SplitItem(
            	        p.getProduct().getProductId(),
            	        p.getProduct().getBrand(),
            	        p.getProduct().getProductName(),
            	        p.getStore().getStoreName(),
            	        p.getPrice()
            	    ))
            	    .toList();

            	StoreStrategy candidate = new StoreStrategy(
            	        store.getStoreName(),
            	        total,
            	        coverage,
            	        productIds.size(),
            	        items,
            	        false
            	);

            	if (best == null || 
            		    candidate.getCoveredCount() > best.getCoveredCount() || 
            		    (candidate.getCoveredCount() == best.getCoveredCount() && total.compareTo(best.getTotal()) < 0)) {
            		    
            		    best = candidate;
            		}
        }

        return best;
    }
}
