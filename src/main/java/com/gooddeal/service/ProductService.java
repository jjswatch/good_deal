package com.gooddeal.service;

import java.time.LocalDateTime;
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
    	LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        return mapToDTO(productRepo.findHotProductsRaw(sevenDaysAgo));
    }

    /**
     * ⭐ 首頁推薦（不會空白）
     */
    public List<HotProductDTO> getFeaturedProducts() {
    	
    	LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);

        List<HotProductDTO> result = new ArrayList<>();

        // 1️⃣ 真熱門
        addIfNotExists(result, mapToDTO(productRepo.findHotProductsRaw(sevenDaysAgo)), 6);

        // 2️⃣ 次熱門（資料少時補齊）
        if (result.size() < 6) {
            addIfNotExists(result, mapToDTO(productRepo.findWarmProductsRaw(sevenDaysAgo)), 6);
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
            	Integer productId = (r[0] != null) ? ((Number) r[0]).intValue() : 0;
                String productName = (String) r[1];
                String brand = (String) r[2];
                String spec = (String) r[3];
                String imageUrl = (String) r[4];
                Long storeCount = (r[5] != null) ? ((Number) r[5]).longValue() : 0L;
                Long reportCount = (r[6] != null) ? ((Number) r[6]).longValue() : 0L;
                Integer minPrice = (r[7] != null) ? ((Number) r[7]).intValue() : 0;

                list.add(new HotProductDTO(
                    productId, productName, brand, spec, imageUrl, 
                    storeCount, reportCount, minPrice
                ));
            } catch (Exception e) {
            	System.err.println("HotProductDTO 轉換出錯，原始資料內容: " + java.util.Arrays.toString(r));
                e.printStackTrace();
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
            p.getBrand(),
            p.getSpec(),
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
