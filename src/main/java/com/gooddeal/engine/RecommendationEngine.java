package com.gooddeal.engine;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import com.gooddeal.dto.SplitStrategy;
import com.gooddeal.dto.StoreStrategy;
import com.gooddeal.dto.StrategyType;

@Service
public class RecommendationEngine {
	
	private static final BigDecimal COST_PER_STOP = BigDecimal.valueOf(20);
	// 提高懲罰，確保缺貨時分數暴增
	private static final BigDecimal MISSING_ITEM_PENALTY = BigDecimal.valueOf(500);
	// 將門檻調高為 1.0，代表 100% 覆蓋
    private static final double MIN_COVERAGE_THRESHOLD = 1.0;
    // 分開買必須比單一店家便宜超過這個數值，才會推薦分開買
    private static final BigDecimal SPLIT_SAVINGS_THRESHOLD = BigDecimal.valueOf(100);

    public StrategyType decide(
            SplitStrategy split,
            StoreStrategy oneStore,
            StoreStrategy preferred
    ) {
    	boolean preferredIsViable = isViable(preferred);
        boolean oneStoreIsViable = isViable(oneStore);

        // 1. 如果完全沒有任何一家店能買齊，直接回傳「分開買」
        if (!oneStoreIsViable && !preferredIsViable) {
            return StrategyType.SPLIT;
        }

        // 2. 計算兩者的得分 (僅針對買齊的情況)
        BigDecimal oneStoreScore = oneStoreIsViable ? calculateStoreScore(oneStore) : BigDecimal.valueOf(Double.MAX_VALUE);
        BigDecimal preferredScore = preferredIsViable ? 
                calculateStoreScore(preferred).subtract(BigDecimal.valueOf(30)) : 
                BigDecimal.valueOf(Double.MAX_VALUE);

        // ✨ 變數 2：使用 bestOneStopScore 來輔助後續判斷 (或用於 Log 紀錄)
        BigDecimal bestOneStopScore = oneStoreScore.min(preferredScore);

        // 3. 檢查「分開買」能省多少實質金額
        BigDecimal realSavings = oneStore.getTotal().subtract(split.getTotal());
        
        // 4. 決策邏輯
        // 如果省下的錢沒超過門檻 (100元)，我們偏好「一站購齊」
        if (realSavings.compareTo(SPLIT_SAVINGS_THRESHOLD) < 0) {
            return (preferredScore.compareTo(oneStoreScore) <= 0) ? 
                    StrategyType.PREFERRED_STORE : StrategyType.ONE_STORE;
        }

        // 如果省超過 100 元，即使能買齊也叫你分開跑
        return StrategyType.SPLIT;
    }
    
    private boolean isViable(StoreStrategy strategy) {
    	if (strategy == null) return false;
    	double currentCoverage = (double) strategy.getCoveredCount() / strategy.getTotalCount();
        return currentCoverage >= MIN_COVERAGE_THRESHOLD;
    }
    
    private BigDecimal calculateStoreScore(StoreStrategy strategy) {
        int missingCount = strategy.getTotalCount() - strategy.getCoveredCount();
        BigDecimal penalty = MISSING_ITEM_PENALTY.multiply(BigDecimal.valueOf(missingCount));

        return strategy.getTotal().add(COST_PER_STOP).add(penalty);
    }

    public String message(SplitStrategy split, StoreStrategy oneStore, StoreStrategy preferred, StrategyType type) {
        BigDecimal savings = oneStore.getTotal().subtract(split.getTotal());
        int compareResult = savings.compareTo(BigDecimal.ZERO);
        
        return switch (type) {
        	case PREFERRED_STORE -> {
        		if (compareResult == 0) {
        			yield "太棒了！你常去的店就是最划算的選擇，且能「一次買齊」所有商品。";
        		}
        		yield "這家是你常去的店，雖然多花一點點錢，但能「一次買齊」最省心！";
        	}
        
        	case ONE_STORE -> {
        		if (compareResult == 0) {
        			yield "這家店就是市場最低價！在一站就能「購齊全餐」，省錢更省時間。";
        		}
        		yield String.format("雖然分開採買能再省 $%s，但在這家店「一次買齊」可以省去奔波勞碌。", savings);
        	}

        	case SPLIT -> {
        		if (oneStore.getCoveredCount() < oneStore.getTotalCount()) {
        			yield "目前沒有單一店家能買齊所有商品，建議分開採買以確保貨源。";
        		}
        		yield String.format("雖然有店家能買齊，但分開買能幫你省下不少錢 ($%s)，推薦分開採購！", savings);
        	}
        
        	default -> "已為您找出目前最平衡的採買方案。";
        };
    }
}