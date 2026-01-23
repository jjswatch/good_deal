package com.gooddeal.service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
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

@Service
public class BasketStrategyService {

    @Autowired
    private ProductPricesRepository priceRepo;

    public BasketStrategyResult compareBasket(List<Integer> productIds) {

        List<ProductPrices> prices =
                priceRepo.findPricesForBasket(productIds);

        if (prices.isEmpty()) {
            return new BasketStrategyResult(
                    StrategyType.NONE,
                    "目前沒有足夠的價格資料",
                    null,
                    null
            );
        }

        /* ========= 1️⃣ 拆單買 ========= */
        Map<Integer, ProductPrices> cheapestPerProduct =
                prices.stream()
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
        Map<Stores, List<ProductPrices>> byStore =
                prices.stream()
                      .collect(Collectors.groupingBy(ProductPrices::getStore));

        StoreStrategy bestStore = null;
        int maxCovered = 0;

        for (var entry : byStore.entrySet()) {

            // 該店家能買到幾個商品（去重）
            int coveredCount = entry.getValue().stream()
                    .map(pp -> pp.getProduct().getProductId())
                    .collect(Collectors.toSet())
                    .size();

            BigDecimal total = entry.getValue().stream()
                    .map(ProductPrices::getPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // 優先比較：覆蓋商品數量，其次比較總價
            if (
                bestStore == null ||
                coveredCount > maxCovered ||
                (coveredCount == maxCovered && total.compareTo(bestStore.getTotal()) < 0)
            ) {
                maxCovered = coveredCount;
                bestStore = new StoreStrategy(
                        entry.getKey().getStoreName(),
                        total,
                        coveredCount,
                        productIds.size()
                );
            }
        }


        /* ========= 3️⃣ 推薦策略 ========= */
        StrategyType recommend;
        String recommendation = buildRecommendation(splitTotal, bestStore);

        if (bestStore == null) {
            recommend = StrategyType.SPLIT;
            recommendation = "部分商品無法在同一店家購齊，建議拆單購買";
        } else {
            BigDecimal diff = bestStore.getTotal().subtract(splitTotal);
            if (diff.compareTo(BigDecimal.ZERO) > 0) {
                recommend = StrategyType.SPLIT;
                recommendation = "拆單購買可再省 $" + diff;
            } else {
                recommend = StrategyType.ONE_STORE;
                recommendation = "單一店家購足最方便";
            }
        }

        return new BasketStrategyResult(
                recommend,
                recommendation,
                splitStrategy,
                bestStore
        );
    }

    private String buildRecommendation(
            BigDecimal splitTotal,
            StoreStrategy bestStore
    ) {
        if (bestStore == null) {
            return "目前沒有任何店家能提供足夠的商品資訊";
        }

        // ⭐ 情況 1：一站購齊但不完整
        if (bestStore.getCoveredCount() < bestStore.getTotalCount()) {
            return "「" + bestStore.getStoreName() + "」可買到 "
                    + bestStore.getCoveredCount() + " / "
                    + bestStore.getTotalCount()
                    + " 項商品，仍最接近一站購齊";
        }

        // ⭐ 情況 2：完整一站購齊 → 比價格
        BigDecimal diff =
                bestStore.getTotal().subtract(splitTotal);

        if (diff.compareTo(BigDecimal.ZERO) > 0) {
            return "拆單購買可再省 $" + diff;
        } else {
            return "單一店家購足最方便";
        }
    }
}


