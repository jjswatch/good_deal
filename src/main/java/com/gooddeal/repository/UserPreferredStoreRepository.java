package com.gooddeal.repository;

import com.gooddeal.model.UserPreferredStore;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserPreferredStoreRepository
        extends JpaRepository<UserPreferredStore, Integer> {

    List<UserPreferredStore> findByUserUserIdOrderByPriorityAsc(Integer userId);

    Optional<UserPreferredStore> findByUserUserIdAndStoreStoreId(
            Integer userId,
            Integer storeId
    );

    long countByUserUserId(Integer userId);
    
    void deleteByUserUserId(Integer userId);

    void deleteByUserUserIdAndStoreStoreId(Integer userId, Integer storeId);
}

