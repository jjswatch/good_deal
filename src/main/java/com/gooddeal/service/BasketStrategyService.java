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
	
	private static final BigDecimal PREFERRED_STORE_BONUS = new BigDecimal("15");
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

        /* ========= 1️⃣ 拆單買 (Lambda 內使用 finalPreferredStoreIds) ========= */
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

        /* ========= 2️⃣ 一站購齊 (整合加權評分邏輯) ========= */
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
            
            // 喜愛優先度 (index 越小越優先)
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

            // --- 加權邏輯計算 ---
            boolean candidateIsPreferred = finalPreferredStoreIds.contains(currentStoreId);
            boolean currentBestIsPreferred = (bestStoreId != null) && finalPreferredStoreIds.contains(bestStoreId);

            // 計算當前候選店家的加權分數 (真實總價 - 獎金 * 涵蓋商品數)
            BigDecimal candidateWeightedScore = total;
            if (candidateIsPreferred) {
                candidateWeightedScore = candidateWeightedScore.subtract(PREFERRED_STORE_BONUS.multiply(new BigDecimal(candidate.getCoveredCount())));
            }

            // 計算目前最優店家的加權分數
            BigDecimal currentBestWeightedScore = (bestStore == null) ? BigDecimal.ZERO : bestStore.getTotal();
            if (bestStore != null && currentBestIsPreferred) {
                currentBestWeightedScore = currentBestWeightedScore.subtract(PREFERRED_STORE_BONUS.multiply(new BigDecimal(bestStore.getCoveredCount())));
            }

            // --- 判定最優店家 ---
            if (bestStore == null 
                    // 1. 商品數較多者優先勝出
                    || candidate.getCoveredCount() > bestStore.getCoveredCount()
                    // 2. 商品數相同時，比較加權得分 (這會讓喜愛店家在價格接近時勝出)
                    || (candidate.getCoveredCount() == bestStore.getCoveredCount() && candidateWeightedScore.compareTo(currentBestWeightedScore) < 0)
                    // 3. 加權得分也一樣時，看喜愛順位 (優先度高的勝出)
                    || (candidate.getCoveredCount() == bestStore.getCoveredCount() && candidateWeightedScore.compareTo(currentBestWeightedScore) == 0 && candidatePriority < bestPriority)
            ) {
                    bestStore = candidate;
                    bestStoreId = currentStoreId;
            }
        }

        /* ========= 3️⃣ 推薦策略輸出 ========= */
        StrategyType recommend;
        String recommendation;
        long storeCount = splitItems.stream().map(SplitItem::getStoreName).distinct().count();

        if (bestStore == null) {
            recommend = StrategyType.SPLIT;
            recommendation = "無法在單一店家購齊，建議分開購買（共需跑 " + storeCount + " 家店）";
        } else {
            BigDecimal moneySavedBySplitting = bestStore.getTotal().subtract(splitTotal);
            BigDecimal totalTravelPenalty = TRAVEL_COST_PER_STORE.multiply(new BigDecimal(storeCount - 1));
            
            boolean isPreferred = finalPreferredStoreIds.contains(bestStoreId);
            String storeName = (isPreferred ? "★ " : "") + bestStore.getStoreName();

            if (storeCount <= 1) {
                recommend = StrategyType.ONE_STORE;
                recommendation = "所有商品在「" + storeName + "」買就是最划算的！";
            } else if (moneySavedBySplitting.compareTo(totalTravelPenalty) < 0) {
                recommend = StrategyType.ONE_STORE;
                recommendation = "雖然拆單可省 $" + moneySavedBySplitting + "，但考量時間成本，在「" + storeName + "」一站購足更省力";
            } else {
                recommend = StrategyType.SPLIT;
                recommendation = "分開買可省下 $" + moneySavedBySplitting + "（需跑 " + storeCount + " 家店），適合精打細算的你";
            }
            
            if (bestStore.getCoveredCount() < bestStore.getTotalCount()) {
                recommendation += "（註：一站購齊缺 " + (bestStore.getTotalCount() - bestStore.getCoveredCount()) + " 項）";
            }
        }
        return new BasketStrategyResult(recommend, recommendation, splitStrategy, bestStore);
    }
}