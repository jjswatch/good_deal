package com.gooddeal.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gooddeal.dto.BasketStrategyResult;
import com.gooddeal.dto.SplitItem;
import com.gooddeal.dto.SplitStrategy;
import com.gooddeal.dto.StoreStrategy;
import com.gooddeal.dto.StrategyType;
import com.gooddeal.model.ProductPrices;
import com.gooddeal.model.Stores;
import com.gooddeal.repository.ProductPricesRepository;
import com.gooddeal.repository.UserPreferredStoreRepository;

@Service
public class BasketStrategyService {
	
	private static final BigDecimal PREFERRED_STORE_BONUS = new BigDecimal("100");
	private static final BigDecimal TRAVEL_COST_PER_STORE = new BigDecimal("30");

    @Autowired
    private ProductPricesRepository priceRepo;
    
    @Autowired
    private UserPreferredStoreRepository preferredRepo;

    public BasketStrategyResult compareBasket(List<Integer> productIds, Integer userId) {
    	List<ProductPrices> prices = priceRepo.findPricesForBasket(productIds);
        
    	final List<Integer> finalPreferredStoreIds;
        if (userId != null) {
        	finalPreferredStoreIds = preferredRepo.findByUserUserIdOrderByPriorityAsc(userId)
                    .stream()
                    .map(ups -> ups.getStore().getStoreId())
                    .toList();
        } else {
            finalPreferredStoreIds = new ArrayList<>();
        }

        if (prices.isEmpty()) {
            return new BasketStrategyResult(StrategyType.NONE, "目前沒有足夠的價格資料", null, null);
        }

        /* ========= 1️⃣ 拆單買 (計算每件商品的最佳選擇) ========= */
        Map<Integer, ProductPrices> cheapestPerProduct = prices.stream()
                .collect(Collectors.groupingBy(
                    pp -> pp.getProduct().getProductId(),
                    Collectors.collectingAndThen(
                        Collectors.minBy((p1, p2) -> {
                            BigDecimal score1 = p1.getPrice();
                            BigDecimal score2 = p2.getPrice();

                            if (finalPreferredStoreIds.contains(p1.getStore().getStoreId())) {
                                score1 = score1.subtract(PREFERRED_STORE_BONUS);
                            }
                            if (finalPreferredStoreIds.contains(p2.getStore().getStoreId())) {
                                score2 = score2.subtract(PREFERRED_STORE_BONUS);
                            }
                            return score1.compareTo(score2);
                        }),
                        Optional::get
                    )
                ));

        List<SplitItem> splitItems = cheapestPerProduct.values().stream()
                .map(pp -> new SplitItem(
                        pp.getProduct().getProductId(),
                        pp.getProduct().getBrand(),
                        pp.getProduct().getProductName(),
                        pp.getStore().getStoreName(),
                        pp.getPrice()
                ))
                .toList();

        BigDecimal splitTotal = splitItems.stream()
                .map(SplitItem::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        SplitStrategy splitStrategy = new SplitStrategy(splitTotal, splitItems);

        /* ========= 2️⃣ 一站購齊 (強力推薦喜愛店家) ========= */
        Map<Integer, String> productNameMap = prices.stream()
                      .map(ProductPrices::getProduct)
                      .distinct()
                      .collect(Collectors.toMap(p -> p.getProductId(), p -> p.getBrand() + " " + p.getProductName()));
        
        Map<Stores, List<ProductPrices>> byStore = prices.stream()
                      .collect(Collectors.groupingBy(ProductPrices::getStore));

        StoreStrategy bestStore = null;
        Integer bestStoreId = null;

        for (var entry : byStore.entrySet()) {
            Stores store = entry.getKey();
            List<ProductPrices> storePrices = entry.getValue();
            Integer currentStoreId = store.getStoreId();
            
            int candidatePriority = finalPreferredStoreIds.indexOf(currentStoreId);
            int bestPriority = (bestStoreId != null) ? finalPreferredStoreIds.indexOf(bestStoreId) : -1;

            Map<Integer, ProductPrices> productMap = storePrices.stream()
                    .collect(Collectors.toMap(pp -> pp.getProduct().getProductId(), pp -> pp, (a, b) -> a));

            List<String> missing = productIds.stream()
                    .filter(pid -> !productMap.containsKey(pid))
                    .map(productNameMap::get)
                    .filter(Objects::nonNull)
                    .toList();

            BigDecimal total = storePrices.stream()
                    .map(ProductPrices::getPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            StoreStrategy candidate = new StoreStrategy(
                    store.getStoreName(), total, productMap.size(), productIds.size(), missing
            );

            boolean candidateIsPreferred = finalPreferredStoreIds.contains(currentStoreId);
            boolean currentBestIsPreferred = (bestStoreId != null) && finalPreferredStoreIds.contains(bestStoreId);

            BigDecimal candidateWeightedScore = total;
            if (candidateIsPreferred) {
                candidateWeightedScore = candidateWeightedScore.subtract(PREFERRED_STORE_BONUS.multiply(new BigDecimal(candidate.getCoveredCount())));
            }

            BigDecimal currentBestWeightedScore = (bestStore == null) ? BigDecimal.ZERO : bestStore.getTotal();
            if (bestStore != null && currentBestIsPreferred) {
                currentBestWeightedScore = currentBestWeightedScore.subtract(PREFERRED_STORE_BONUS.multiply(new BigDecimal(bestStore.getCoveredCount())));
            }

            // ⭐ 優化判定邏輯：強制提升喜愛店家的權限
            if (bestStore == null 
                    // A. 覆蓋率更高 (這是不變的原則，買得到東西最重要)
                    || candidate.getCoveredCount() > bestStore.getCoveredCount()
                    // B. 覆蓋率相同，但候選者是喜愛店家，而目前的不是
                    || (candidate.getCoveredCount() == bestStore.getCoveredCount() && candidateIsPreferred && !currentBestIsPreferred)
                    // C. 覆蓋率相同且同樣是喜愛/非喜愛店家，則比較加權後的分數
                    || (candidate.getCoveredCount() == bestStore.getCoveredCount() && (candidateIsPreferred == currentBestIsPreferred) && candidateWeightedScore.compareTo(currentBestWeightedScore) < 0)
                    // D. 分數也一樣，則看排序順位
                    || (candidate.getCoveredCount() == bestStore.getCoveredCount() && candidateWeightedScore.compareTo(currentBestWeightedScore) == 0 && candidatePriority < bestPriority)
            ) {
                    bestStore = candidate;
                    bestStoreId = currentStoreId;
            }
        }

        /* ========= 3️⃣ 推薦策略輸出 (優化文案) ========= */
        StrategyType recommend;
        String recommendation;
        long storeCount = splitItems.stream().map(SplitItem::getStoreName).distinct().count();

        if (bestStore == null) {
            recommend = StrategyType.SPLIT;
            recommendation = "目前沒有任何一家店有這些商品，建議分開購買（共需跑 " + storeCount + " 家店）";
        } else {
            BigDecimal moneySavedBySplitting = bestStore.getTotal().subtract(splitTotal);
            BigDecimal totalTravelPenalty = TRAVEL_COST_PER_STORE.multiply(new BigDecimal(storeCount - 1));
            
            boolean isPreferred = finalPreferredStoreIds.contains(bestStoreId);
            String storeName = (isPreferred ? "★ " : "") + bestStore.getStoreName();

            // 判斷價格是否相同或拆單其實只有一家店
            if (moneySavedBySplitting.compareTo(BigDecimal.ZERO) == 0 || storeCount <= 1) {
                recommend = StrategyType.ONE_STORE;
                recommendation = "所有商品在「" + storeName + "」買最划算！價格與拆單相同，且免去奔波之苦。";
            } 
            else if (moneySavedBySplitting.compareTo(totalTravelPenalty) < 0) {
                recommend = StrategyType.ONE_STORE;
                recommendation = "雖然拆單可省 $" + moneySavedBySplitting + "，但考量跨店跑 " + storeCount + " 家的時間，在您喜愛的「" + storeName + "」一站購齊更省心";
            } 
            else {
                recommend = StrategyType.SPLIT;
                recommendation = "價格優先推薦！分開買可省下 $" + moneySavedBySplitting + "（共需跑 " + storeCount + " 家店），適合精打細算的您";
            }
            
            if (bestStore.getCoveredCount() < bestStore.getTotalCount()) {
                recommendation += "（註：該店目前缺少 " + (bestStore.getTotalCount() - bestStore.getCoveredCount()) + " 項商品）";
            }
        }
        return new BasketStrategyResult(recommend, recommendation, splitStrategy, bestStore);
    }
}