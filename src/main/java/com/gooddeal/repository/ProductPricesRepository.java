package com.gooddeal.repository;

import com.gooddeal.model.ProductPrices;
import com.gooddeal.model.Products;
import com.gooddeal.model.Stores;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface ProductPricesRepository extends JpaRepository<ProductPrices, Integer> {

    List<ProductPrices> findByProductProductId(Integer productId);

    List<ProductPrices> findByStoreStoreId(Integer storeId);
    
    List<ProductPrices> findByStoreStoreGroup(Integer storeGroup);
    
 // ⭐ 歷史最低（不限時間）
    ProductPrices findFirstByProductProductIdOrderByPriceAscUpdatedAtDesc(Integer productId);

    // ⭐ 近 30 天最低
    ProductPrices findFirstByProductProductIdAndUpdatedAtAfterOrderByPriceAscUpdatedAtDesc(
            Integer productId, 
            LocalDateTime from
    );
    
    ProductPrices findTopByProductAndStoreOrderByPriceDateDesc(
            Products product,
            Stores store
    );

    @Query("""
            SELECT pp
            FROM ProductPrices pp
            WHERE pp.product.productId IN :productIds
        """)
        List<ProductPrices> findPricesForBasket(List<Integer> productIds);
}
