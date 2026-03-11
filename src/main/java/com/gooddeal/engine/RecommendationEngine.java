package com.gooddeal.engine;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import com.gooddeal.dto.SplitItem;
import com.gooddeal.dto.SplitStrategy;
import com.gooddeal.dto.StoreStrategy;
import com.gooddeal.dto.StrategyType;

@Service
public class RecommendationEngine {
	
	private static final BigDecimal COST_PER_STOP = BigDecimal.valueOf(20);
    // 提高懲罰，或改用補位法
    private static final BigDecimal MISSING_ITEM_PENALTY = BigDecimal.valueOf(200); 
    // 設定最低覆蓋率門檻 (例如必須買到 70% 的商品)
    private static final double MIN_COVERAGE_THRESHOLD = 0.7;
    // 分開買必須比單一店家便宜超過這個數值，才會推薦分開買
    private static final BigDecimal SPLIT_SAVINGS_THRESHOLD = BigDecimal.valueOf(100);

    public StrategyType decide(
            SplitStrategy split,
            StoreStrategy oneStore,
            StoreStrategy preferred
    ) {
    	boolean preferredIsViable = isViable(preferred);
        boolean oneStoreIsViable = isViable(oneStore);

        // 1. 計算原始得分
        long splitStoreCount = split.getItems().stream().map(SplitItem::getStoreName).distinct().count();
        BigDecimal splitScore = split.getTotal().add(COST_PER_STOP.multiply(BigDecimal.valueOf(splitStoreCount)));

        BigDecimal oneStoreScore = oneStoreIsViable ? calculateStoreScore(oneStore) : BigDecimal.valueOf(Double.MAX_VALUE);
        BigDecimal preferredScore = preferredIsViable ? 
                calculateStoreScore(preferred).subtract(BigDecimal.valueOf(30)) : 
                BigDecimal.valueOf(Double.MAX_VALUE);

        // 2. ✨ 核心邏輯修改：優先考慮單店或偏好店
        // 找出單一採買（包含偏好店）的最優方案
        BigDecimal bestOneStopScore = oneStoreScore.min(preferredScore);
        
        // 3. ✨ 比較「分開買」與「一站購」的實質價差
        // 如果一站購可行，且 (分開買的總價) 並沒有比 (一站購總價) 便宜超過門檻
        if (bestOneStopScore.compareTo(BigDecimal.valueOf(Double.MAX_VALUE)) < 0) {
            BigDecimal realSavings = oneStore.getTotal().subtract(split.getTotal());
            
            // 如果省不到 50 元，強迫選擇一站購方案
            if (realSavings.compareTo(SPLIT_SAVINGS_THRESHOLD) < 0) {
                return (preferredScore.compareTo(oneStoreScore) <= 0) ? 
                        StrategyType.PREFERRED_STORE : StrategyType.ONE_STORE;
            }
        }

        // 4. 若價差真的很大，才走原本的評分邏輯
        BigDecimal minScore = splitScore.min(oneStoreScore).min(preferredScore);
        if (minScore.equals(preferredScore)) return StrategyType.PREFERRED_STORE;
        if (minScore.equals(oneStoreScore)) return StrategyType.ONE_STORE;
        return StrategyType.SPLIT;
    }
    
    private boolean isViable(StoreStrategy strategy) {
        if (strategy == null) return false;
        double coverage = (double) strategy.getCoveredCount() / strategy.getTotalCount();
        return coverage >= MIN_COVERAGE_THRESHOLD;
    }
    
    private BigDecimal calculateStoreScore(StoreStrategy strategy) {
        int missingCount = strategy.getTotalCount() - strategy.getCoveredCount();
        // 核心改動：缺貨懲罰必須大於該商品可能的價差，否則無法阻止系統選錯
        BigDecimal penalty = MISSING_ITEM_PENALTY.multiply(BigDecimal.valueOf(missingCount));

        return strategy.getTotal()
                .add(COST_PER_STOP)
                .add(penalty);
    }

    public String message(SplitStrategy split, StoreStrategy oneStore, StoreStrategy preferred, StrategyType type) {
        // 修正價格失真：節省金額應該是 (一站購齊店的總價) - (分開買的總價)
        // 且只有在兩者都買齊（或覆蓋度相同）時才有意義
        BigDecimal savings = oneStore.getTotal().subtract(split.getTotal());
        
        return switch (type) {
            case PREFERRED_STORE -> String.format("這家是你常去的店，且能買到大部分商品 (%d/%d)，最方便！", 
                                    preferred.getCoveredCount(), preferred.getTotalCount());
            case ONE_STORE -> "雖然分開買更便宜，但在這家一次買齊最省時間。";
            case SPLIT -> savings.compareTo(BigDecimal.ZERO) > 0 ? 
                          String.format("這些商品在不同店家價差較大，分開買可省下 $%s。", savings) :
                          "建議分開採買，能確保買齊所有商品且價格最划算。";
            default -> "已為您找出最佳採買方案";
        };
    }
}