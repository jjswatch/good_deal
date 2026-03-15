package com.gooddeal.repository;

import com.gooddeal.dto.FavoriteProjection;
import com.gooddeal.model.UserFavorites;

import jakarta.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserFavoritesRepository extends JpaRepository<UserFavorites, Integer> {

    List<UserFavorites> findByUserUserId(Integer userId);
    
    boolean existsByUserUserIdAndProductProductId(Integer userId, Integer productId);
    
    @Transactional
    @Modifying
    void deleteByUserUserIdAndProductProductId(Integer userId, Integer productId);
    
    @Query(value = """
            SELECT 
                f.favorite_id AS favoriteId, 
                p.product_id AS productId, 
                p.product_name AS productName, 
                p.brand AS brand, 
                p.image_url AS imageUrl,
                (SELECT MIN(pp.price) FROM product_prices pp 
                 WHERE pp.product_id = p.product_id) AS lowestPrice,
                (SELECT s.store_name FROM product_prices pp 
                 JOIN stores s ON pp.store_id = s.store_id 
                 WHERE pp.product_id = p.product_id 
                 ORDER BY pp.price ASC, pp.updated_at DESC LIMIT 1) AS bestStoreName,
                f.created_at AS createdAt
            FROM user_favorites f
            JOIN products p ON f.product_id = p.product_id
            WHERE f.user_id = :userId
            ORDER BY f.created_at DESC
            """, nativeQuery = true)
        List<FavoriteProjection> findFavoritesWithBestPrice(@Param("userId") Integer userId);
}
