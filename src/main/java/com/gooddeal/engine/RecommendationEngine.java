package com.gooddeal.engine;

import org.springframework.stereotype.Service;

import com.gooddeal.dto.SplitStrategy;
import com.gooddeal.dto.StoreStrategy;
import com.gooddeal.dto.StrategyType;

@Service
public class RecommendationEngine {

    public StrategyType decide(
            SplitStrategy split,
            StoreStrategy oneStore,
            StoreStrategy preferred
    ) {

    	if (preferred != null) {
            return StrategyType.PREFERRED_STORE;
        }

    	if (oneStore != null && oneStore.getCoveredCount() >= split.getItems().size()) {
            if (oneStore.getTotal().subtract(split.getTotal()).doubleValue() < 50) {
                return StrategyType.ONE_STORE;
            }
        }

    	return StrategyType.SPLIT;
    }

    public String message(SplitStrategy split, StoreStrategy oneStore, StoreStrategy preferred, StrategyType type) {
        return switch (type) {
            case PREFERRED_STORE -> "這家是你常去的店，雖然稍微貴一點點，但最方便！";
            case ONE_STORE -> "雖然分開買更便宜，但在這家一次買齊最省時間。";
            case SPLIT -> "這些商品在不同店家價差較大，建議分開採買最划算。";
            default -> "已為您找出最佳採買方案";
        };
    }
}