package com.gooddeal.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.gooddeal.dto.HotProductDTO;
import com.gooddeal.repository.ProductsRepository;

@Service
public class ProductService {

    private final ProductsRepository productRepo;

    public ProductService(ProductsRepository productRepo) {
        this.productRepo = productRepo;
    }

    public List<HotProductDTO> getHotProducts() {
        List<Object[]> results = productRepo.findHotProductsRaw();
        return results.stream()
            .map(r -> {
                try {
                    return new HotProductDTO(
                        r[0] != null ? ((Number) r[0]).intValue() : 0,
                        (String) r[1],
                        (String) r[2],
                        r[3] != null ? ((Number) r[3]).longValue() : 0L,
                        r[4] != null ? ((Number) r[4]).longValue() : 0L,
                        r[5] != null ? ((Number) r[5]).intValue() : 0
                    );
                } catch (Exception e) {
                    System.err.println("轉換資料列失敗: " + e.getMessage());
                    return null;
                }
            })
            .filter(dto -> dto != null) // 過濾掉轉換失敗的
            .toList();
    }
}
