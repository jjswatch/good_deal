package com.gooddeal.engine;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.stereotype.Service;

import com.gooddeal.dto.SplitStrategy;
import com.gooddeal.dto.StoreStrategy;
import com.gooddeal.dto.StrategyType;

@Service
public class RecommendationEngine {
	
	private static final BigDecimal COST_PER_STOP = BigDecimal.valueOf(20);
    private static final BigDecimal MISSING_ITEM_PENALTY = BigDecimal.valueOf(500);
    private static final BigDecimal SPLIT_SAVINGS_THRESHOLD = BigDecimal.valueOf(100);

    public StrategyType decide(SplitStrategy split, StoreStrategy oneStore, StoreStrategy preferred) {
        boolean preferredIsViable = isViable(preferred);
        boolean oneStoreIsViable = isViable(oneStore);

        // 1. 如果單一店家都買不齊，只能分開買
        if (!oneStoreIsViable && !preferredIsViable) {
            return StrategyType.SPLIT;
        }

        // 2. 計算單一店家的得分（含一次路程費 $20）
        BigDecimal oneStoreScore = oneStoreIsViable ? 
            oneStore.getTotal().add(COST_PER_STOP) : BigDecimal.valueOf(Double.MAX_VALUE);
            
        // 常去店給予 $30 的「心理加成」優惠（也就是願意為了它多花30元）
        BigDecimal preferredScore = preferredIsViable ? 
            preferred.getTotal().add(COST_PER_STOP).subtract(BigDecimal.valueOf(30)) : 
            BigDecimal.valueOf(Double.MAX_VALUE);

        // 3. ✨ 計算「分開買」的真實經濟成本（含多家店的路程費）
        // 例如跑三家店就是 3 * $20 = $60
        BigDecimal splitTravelCost = COST_PER_STOP.multiply(BigDecimal.valueOf(split.getUniqueStoreCount()));
        BigDecimal splitTotalEconomicCost = split.getTotal().add(splitTravelCost);

        // 4. 決策邏輯：比較「最划算的單一店」與「分開買」
        BigDecimal bestOneStopPrice = oneStoreIsViable ? oneStore.getTotal() : preferred.getTotal();
        BigDecimal realSavings = bestOneStopPrice.subtract(split.getTotal());

        // 如果「分開買」省下的錢沒超過門檻 (100元)，優先選一站購齊
        if (realSavings.compareTo(SPLIT_SAVINGS_THRESHOLD) < 0) {
            return (preferredScore.compareTo(oneStoreScore) <= 0) ? 
                    StrategyType.PREFERRED_STORE : StrategyType.ONE_STORE;
        }

        return StrategyType.SPLIT;
    }
    
    private boolean isViable(StoreStrategy strategy) {
    	if (strategy == null) return false;
    	return strategy.getCoveredCount() == strategy.getTotalCount();
    }
    
    private BigDecimal calculateStoreScore(StoreStrategy strategy) {
        int missingCount = strategy.getTotalCount() - strategy.getCoveredCount();
        BigDecimal penalty = MISSING_ITEM_PENALTY.multiply(BigDecimal.valueOf(missingCount));

        return strategy.getTotal().add(COST_PER_STOP).add(penalty);
    }

    public String message(SplitStrategy split, StoreStrategy oneStore, StoreStrategy preferred, StrategyType type) {
        BigDecimal savings = oneStore.getTotal().subtract(split.getTotal());
        int compareResult = savings.compareTo(BigDecimal.ZERO);
        int savingsInt = savings.setScale(0, RoundingMode.HALF_UP).intValue();
        
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
        		yield String.format("雖然分開採買能再省 $%d，但在這家店「一次買齊」可以省去奔波勞碌。", savingsInt);
        	}

        	case SPLIT -> {
        		if (oneStore.getCoveredCount() < oneStore.getTotalCount()) {
        			yield "目前沒有單一店家能買齊所有商品，建議分開採買以確保貨源。";
        		}
        		yield String.format("雖然有店家能買齊，但分開買能幫你省下不少錢 ($%s)，推薦分開採購！", savingsInt);
        	}
        
        	default -> "已為您找出目前最平衡的採買方案。";
        };
    }
}