package com.gooddeal.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.gooddeal.dto.HotProductDTO;
import com.gooddeal.model.Products;
import com.gooddeal.repository.ProductsRepository;

@Service
public class ProductService {

    private final ProductsRepository productRepo;

    public ProductService(ProductsRepository productRepo) {
        this.productRepo = productRepo;
    }

    /**
     * 🔥 舊版：只取嚴格熱門（保留）
     */
    public List<HotProductDTO> getHotProducts() {
        return mapToDTO(productRepo.findHotProductsRaw());
    }

    /**
     * ⭐ 首頁推薦（不會空白）
     */
    public List<HotProductDTO> getFeaturedProducts() {

        List<HotProductDTO> result = new ArrayList<>();

        // 1️⃣ 真熱門
        addIfNotExists(result, mapToDTO(productRepo.findHotProductsRaw()), 6);

        // 2️⃣ 次熱門（資料少時補齊）
        if (result.size() < 6) {
            addIfNotExists(result, mapToDTO(productRepo.findWarmProductsRaw()), 6);
        }

        // 3️⃣ 最新商品（最後保底）
        if (result.size() < 6) {
            List<Products> latest = productRepo.findTop5ByOrderByCreatedAtDesc();
            for (Products p : latest) {
                if (result.size() >= 6) break;
                if (result.stream().noneMatch(r -> r.getProductId().equals(p.getProductId()))) {
                    result.add(toDTO(p));
                }
            }
        }

        return result;
    }

    // =========================
    // 🔧 Helper Methods
    // =========================

    /**
     * Native SQL → DTO
     */
    private List<HotProductDTO> mapToDTO(List<Object[]> rows) {
        List<HotProductDTO> list = new ArrayList<>();

        for (Object[] r : rows) {
            try {
                list.add(new HotProductDTO(
                    r[0] != null ? ((Number) r[0]).intValue() : 0,
                    (String) r[1],
                    (String) r[2],
                    r[3] != null ? ((Number) r[3]).longValue() : 0L,
                    r[4] != null ? ((Number) r[4]).longValue() : 0L,
                    r[5] != null ? ((Number) r[5]).intValue() : 0
                ));
            } catch (Exception e) {
                System.err.println("HotProductDTO 轉換失敗: " + e.getMessage());
            }
        }

        return list;
    }

    /**
     * Entity → DTO（最新商品保底用）
     */
    private HotProductDTO toDTO(Products p) {
        return new HotProductDTO(
            p.getProductId(),
            p.getProductName(),
            p.getImageUrl(),
            0L,     // storeCount（未知）
            0L,     // reportCount（未知）
            0       // minPrice（未知）
        );
    }

    /**
     * 補齊資料（避免重複）
     */
    private void addIfNotExists(
            List<HotProductDTO> target,
            List<HotProductDTO> source,
            int limit
    ) {
        for (HotProductDTO dto : source) {
            if (target.size() >= limit) break;

            boolean exists = target.stream()
                .anyMatch(t -> t.getProductId().equals(dto.getProductId()));

            if (!exists) {
                target.add(dto);
            }
        }
    }
}
