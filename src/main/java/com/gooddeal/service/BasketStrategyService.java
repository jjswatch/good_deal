package com.gooddeal.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gooddeal.dto.BasketStrategyResult;
import com.gooddeal.engine.BasketOptimizationEngine;
import com.gooddeal.model.ProductPrices;
import com.gooddeal.repository.ProductPricesRepository;
import com.gooddeal.repository.UserPreferredStoreRepository;

@Service
public class BasketStrategyService {

    @Autowired
    private ProductPricesRepository priceRepo;

    @Autowired
    private UserPreferredStoreRepository preferredRepo;

    @Autowired
    private BasketOptimizationEngine engine;

    public BasketStrategyResult compareBasket(
            List<Integer> productIds,
            Integer userId
    ) {

        List<ProductPrices> prices =
                priceRepo.findPricesForBasket(productIds);

        List<Integer> preferredStores =
                preferredRepo.findByUserUserIdOrderByPriorityAsc(userId)
                        .stream()
                        .map(p -> p.getStore().getStoreId())
                        .toList();

        return engine.optimize(
                prices,
                productIds,
                preferredStores,
                userId
        );
    }
}