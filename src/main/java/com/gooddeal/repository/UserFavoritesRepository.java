package com.gooddeal.repository;

import com.gooddeal.model.UserFavorites;

import jakarta.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.util.List;

public interface UserFavoritesRepository extends JpaRepository<UserFavorites, Integer> {

    List<UserFavorites> findByUserUserId(Integer userId);
    
    boolean existsByUserUserIdAndProductProductId(Integer userId, Integer productId);
    
    @Transactional
    @Modifying
    void deleteByUserUserIdAndProductProductId(Integer userId, Integer productId);
}
