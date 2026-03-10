package com.gooddeal.engine;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gooddeal.dto.BasketStrategyResult;
import com.gooddeal.dto.SplitStrategy;
import com.gooddeal.dto.StoreStrategy;
import com.gooddeal.dto.StrategyType;
import com.gooddeal.model.ProductPrices;
import com.gooddeal.strategy.OneStoreStrategy;
import com.gooddeal.strategy.PreferredStoreStrategy;
import com.gooddeal.strategy.SplitBasketStrategy;

@Service
public class BasketOptimizationEngine {

    @Autowired
    private SplitBasketStrategy splitStrategy;

    @Autowired
    private OneStoreStrategy oneStoreStrategy;

    @Autowired
    private PreferredStoreStrategy preferredStoreStrategy;

    @Autowired
    private RecommendationEngine recommendationEngine;

    public BasketStrategyResult optimize(
            List<ProductPrices> prices,
            List<Integer> productIds,
            List<Integer> preferredStores,
            Integer userId
    ) {

        SplitStrategy split = splitStrategy.calculate(prices, preferredStores);

        StoreStrategy oneStore = oneStoreStrategy.calculate(prices, productIds);

        StoreStrategy preferredRaw = preferredStoreStrategy.calculate(prices, productIds, preferredStores);
        
        StoreStrategy preferredCandidate = preferredStoreStrategy.recommend(
                preferredRaw, 
                split.getTotal(), 
                userId
        );

        StrategyType recommend = recommendationEngine.decide(split, oneStore, preferredCandidate);
        
        StoreStrategy bestStoreToReturn = (recommend == StrategyType.PREFERRED_STORE) ? preferredRaw : oneStore;

        return new BasketStrategyResult(
                recommend,
                recommendationEngine.message(split, oneStore, preferredRaw, recommend),
                split,
                bestStoreToReturn
        );
    }
}
