package com.gooddeal.repository;

import com.gooddeal.model.UserFavorites;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserFavoritesRepository extends JpaRepository<UserFavorites, Integer> {

    List<UserFavorites> findByUserUserId(Integer userId);
}
