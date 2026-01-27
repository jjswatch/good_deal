package com.gooddeal.service;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.gooddeal.model.Products;
import com.gooddeal.model.UserFavorites;
import com.gooddeal.model.Users;
import com.gooddeal.repository.ProductsRepository;
import com.gooddeal.repository.UserFavoritesRepository;
import com.gooddeal.repository.UsersRepository;

import jakarta.transaction.Transactional;

@Service
public class UserFavoritesService {

    private final UserFavoritesRepository repo;
    private final UsersRepository usersRepo;
    private final ProductsRepository productsRepo;

    public UserFavoritesService(UserFavoritesRepository repo, UsersRepository usersRepo, ProductsRepository productsRepo) {
        this.repo = repo;
        this.usersRepo = usersRepo;
        this.productsRepo = productsRepo;
    }

    @Transactional
    public Map<String, Object> toggleFavorite(Integer userId, Integer productId) {
        boolean exists = repo.existsByUserUserIdAndProductProductId(userId, productId);

        if (exists) {
            // 已存在 -> 移除收藏
            repo.deleteByUserUserIdAndProductProductId(userId, productId);
            return Map.of("status", "removed", "favorited", false);
        } else {
            // 不存在 -> 新增收藏
            Users user = usersRepo.findById(userId).orElseThrow();
            Products product = productsRepo.findById(productId).orElseThrow();

            UserFavorites fav = new UserFavorites();
            fav.setUser(user);
            fav.setProduct(product);
            repo.save(fav);
            return Map.of("status", "added", "favorited", true);
        }
    }
}
