package com.gooddeal.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gooddeal.dto.PriceHistoryRequest;
import com.gooddeal.dto.PriceHistoryResponse;
import com.gooddeal.model.PriceHistory;
import com.gooddeal.model.Products;
import com.gooddeal.model.Stores;
import com.gooddeal.repository.PriceHistoryRepository;
import com.gooddeal.repository.ProductsRepository;
import com.gooddeal.repository.StoresRepository;

import jakarta.transaction.Transactional;

@Service
public class PriceHistoryService {

    @Autowired
    private PriceHistoryRepository priceHistoryRepository;

    @Autowired
    private ProductsRepository productsRepository;

    @Autowired
    private StoresRepository storesRepository;
    
    public List<PriceHistoryResponse> getAllHistory(int limit) {
        // 這裡可以使用 Pageable 來處理 limit
        return priceHistoryRepository.findAllByOrderByChangedAtDesc()
            .stream()
            .limit(limit)
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }

    // --- 取得特定商品的所有歷史價格 ---
    public List<PriceHistoryResponse> getHistoryByProductId(Integer productId) {
        List<PriceHistory> historyList = priceHistoryRepository.findByProductProductIdOrderByChangedAtDesc(productId);
        
        // 將 Entity 轉換為 Response DTO
        return historyList.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    // --- 新增歷史價格紀錄 ---
    @Transactional
    public PriceHistoryResponse createPriceHistory(PriceHistoryRequest request) {
        Products product = productsRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        Stores store = storesRepository.findById(request.getStoreId())
                .orElseThrow(() -> new RuntimeException("Store not found"));

        PriceHistory history = new PriceHistory();
        history.setProduct(product);
        history.setStore(store);
        history.setOldPrice(request.getOldPrice());
        history.setNewPrice(request.getNewPrice());
        // changedAt 會由 @CreationTimestamp 自動生成

        PriceHistory savedHistory = priceHistoryRepository.save(history);
        return convertToResponse(savedHistory);
    }
    
    public PriceHistoryResponse updatePriceHistory(Integer id, PriceHistoryRequest request) {
        PriceHistory entity = priceHistoryRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("紀錄不存在"));
        
        entity.setOldPrice(request.getOldPrice());
        entity.setNewPrice(request.getNewPrice());
        // 如果有連動更新 Product 表的當前價格，邏輯也要寫在這
        
        PriceHistory saved = priceHistoryRepository.save(entity);
        return convertToResponse(saved); // 轉成 DTO 回傳
    }

    // --- 刪除歷史紀錄 (管理員功能) ---
    @Transactional
    public void deletePriceHistory(Integer historyId) {
        if (!priceHistoryRepository.existsById(historyId)) {
            throw new RuntimeException("History record not found");
        }
        priceHistoryRepository.deleteById(historyId);
    }

    // --- 輔助方法：Entity 轉 Response ---
    private PriceHistoryResponse convertToResponse(PriceHistory history) {
        PriceHistoryResponse response = new PriceHistoryResponse();
        response.setHistoryId(history.getHistoryId());
        response.setProductId(history.getProduct().getProductId());
        response.setProductName(history.getProduct().getProductName()); // 假設 Product 有 getName
        response.setStoreId(history.getStore().getStoreId());
        response.setStoreName(history.getStore().getStoreName());       // 假設 Store 有 getName
        response.setOldPrice(history.getOldPrice());
        response.setNewPrice(history.getNewPrice());
        response.setChangedAt(history.getChangedAt());
        return response;
    }
}

