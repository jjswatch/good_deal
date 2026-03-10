package com.gooddeal.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.gooddeal.dto.SplitStrategy;
import com.gooddeal.dto.StoreStrategy;
import com.gooddeal.dto.StrategyCompareResponse;
import com.gooddeal.dto.StrategyType;
import com.gooddeal.engine.RecommendationEngine;
import com.gooddeal.model.ProductPrices;
import com.gooddeal.repository.ProductPricesRepository;
import com.gooddeal.strategy.OneStoreStrategy;
import com.gooddeal.strategy.PreferredStoreStrategy;
import com.gooddeal.strategy.SplitStrategyService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StrategyCompareService {

    private final ProductPricesRepository pricesRepo;

    private final SplitStrategyService splitStrategy;

    private final OneStoreStrategy oneStoreStrategy;
    
    private final PreferredStoreStrategy preferredStrategy;

    private final RecommendationEngine engine;

    public StrategyCompareResponse compareBasket(List<Integer> productIds, Integer userId) {

        List<ProductPrices> prices = pricesRepo.findPricesForBasket(productIds);

        SplitStrategy split = splitStrategy.calculate(prices);
        StoreStrategy oneStore = oneStoreStrategy.calculate(prices, productIds);
        StoreStrategy preferred = preferredStrategy.recommend(oneStore, split.getTotal(), userId);

        StrategyType type = engine.decide(split, oneStore, preferred);
        String message = engine.message(split, oneStore, preferred, type);

        StrategyCompareResponse res = new StrategyCompareResponse();
        res.setSplitStrategy(split);
        res.setStrategyType(type);
        res.setMessage(message);
        if (type == StrategyType.PREFERRED_STORE) {
            res.setBestStore(preferred);
        } else {
            res.setBestStore(oneStore);
        }

        return res;
    }
}