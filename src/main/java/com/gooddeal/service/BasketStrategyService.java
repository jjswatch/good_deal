package com.gooddeal.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
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

    @Autowired
    private ProductPricesRepository priceRepo;
    
    @Autowired
    private UserPreferredStoreRepository preferredRepo;

    public BasketStrategyResult compareBasket(List<Integer> productIds, Integer userId) {
    	List<ProductPrices> prices = priceRepo.findPricesForBasket(productIds);
        
        List<Integer> preferredStoreIds = new ArrayList<>();
        if (userId != null) {
        	preferredStoreIds = preferredRepo.findByUserUserIdOrderByPriorityAsc(userId)
                    .stream()
                    .map(ups -> ups.getStore().getStoreId())
                    .toList();
        }

        if (prices.isEmpty()) {
            return new BasketStrategyResult(StrategyType.NONE, "目前沒有足夠的價格資料", null, null);
        }

        /* ========= 1️⃣ 拆單買 ========= */
        Map<Integer, ProductPrices> cheapestPerProduct = prices.stream()
                      .collect(Collectors.groupingBy(
                          pp -> pp.getProduct().getProductId(),
                          Collectors.collectingAndThen(
                              Collectors.minBy(
                                  Comparator.comparing(ProductPrices::getPrice)
                              ),
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

        SplitStrategy splitStrategy =
                new SplitStrategy(splitTotal, splitItems);

        /* ========= 2️⃣ 一站購齊 ========= */
        Map<Integer, String> productNameMap = prices.stream()
                      .map(ProductPrices::getProduct)
                      .distinct()
                      .collect(Collectors.toMap(
                          p -> p.getProductId(),
                          p -> p.getBrand() + " " + p.getProductName()
                      ));
        
        Map<Stores, List<ProductPrices>> byStore =
                prices.stream()
                      .collect(Collectors.groupingBy(ProductPrices::getStore));

        StoreStrategy bestStore = null;
        Integer bestStoreId = null; // 用於追蹤當前最優店家的 ID

        for (var entry : byStore.entrySet()) {
            Stores store = entry.getKey();
            List<ProductPrices> storePrices = entry.getValue();
            Integer currentStoreId = store.getStoreId();

            // 建立該店商品 Map
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

            // ⭐ 改進的篩選邏輯
            boolean candidateIsPreferred = preferredStoreIds.contains(currentStoreId);
            boolean currentBestIsPreferred = (bestStoreId != null) && preferredStoreIds.contains(bestStoreId);

            if (bestStore == null 
                // 條件 A: 覆蓋更多商品
                || candidate.getCoveredCount() > bestStore.getCoveredCount()
                // 條件 B: 覆蓋數相同，但 candidate 是喜愛店家，原本的不是
                || (candidate.getCoveredCount() == bestStore.getCoveredCount() && candidateIsPreferred && !currentBestIsPreferred)
                // 條件 C: 覆蓋數與喜愛狀態都相同，比價格
                || (candidate.getCoveredCount() == bestStore.getCoveredCount() && candidateIsPreferred == currentBestIsPreferred && candidate.getTotal().compareTo(bestStore.getTotal()) < 0)
            ) {
                bestStore = candidate;
                bestStoreId = currentStoreId;
            }
        }

        /* ========= 3️⃣ 推薦策略 ========= */
        StrategyType recommend;
        String recommendation;

        if (bestStore == null) {
            recommend = StrategyType.SPLIT;
            recommendation = "無法在單一店家購齊，建議拆單購買";
        } else {
            BigDecimal diff = bestStore.getTotal().subtract(splitTotal);
            boolean isPreferred = preferredStoreIds.contains(bestStoreId);
            
            String storeMsg = isPreferred ? "★ 喜愛店家「" + bestStore.getStoreName() + "」" : "「" + bestStore.getStoreName() + "」";
            
            if (diff.compareTo(BigDecimal.ZERO) > 0 && diff.compareTo(new BigDecimal("50")) > 0) {
                // 如果一站購齊比拆單貴超過 50 元，建議拆單
                recommend = StrategyType.SPLIT;
                recommendation = storeMsg + " 購齊需多花 $" + diff + "，建議拆單以省錢";
            } else {
                recommend = StrategyType.ONE_STORE;
                recommendation = storeMsg + " 購足最便利";
            }
        }

        return new BasketStrategyResult(recommend, recommendation, splitStrategy, bestStore);
    }

    
}


