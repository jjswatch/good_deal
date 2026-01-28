package com.gooddeal.controller;

import com.gooddeal.model.UserFavorites;
import com.gooddeal.repository.UserFavoritesRepository;
import com.gooddeal.service.UserFavoritesService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/favorites")
public class UserFavoritesController {

    @Autowired
    private UserFavoritesRepository repo;
    
    @Autowired
    private UserFavoritesService service;

    @GetMapping("/user/{userId}")
    public List<UserFavorites> getByUser(@PathVariable Integer userId) {
        return repo.findByUserUserId(userId);
    }
    
    @GetMapping("/status")
    public Map<String, Boolean> checkStatus(@RequestParam Integer userId, @RequestParam Integer productId) {
        boolean isFavorited = repo.existsByUserUserIdAndProductProductId(userId, productId);
        return Map.of("isFavorited", isFavorited);
    }
    
    @PostMapping("/toggle")
    public Map<String, Object> toggle(@RequestBody Map<String, Integer> body) {
        return service.toggleFavorite(body.get("userId"), body.get("productId"));
    }

    @PostMapping
    public UserFavorites create(@RequestBody UserFavorites fav) {
        return repo.save(fav);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
    	if (repo.existsById(id)) {
            repo.deleteById(id);
        }
    }
    
    @DeleteMapping
    public void deleteByUserAndProduct(
            @RequestParam Integer userId,
            @RequestParam Integer productId
    ) {
    	service.removeFavorite(userId, productId);
    }
}
