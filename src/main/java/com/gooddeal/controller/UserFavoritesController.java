package com.gooddeal.controller;

import com.gooddeal.model.UserFavorites;
import com.gooddeal.repository.UserFavoritesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/favorites")
public class UserFavoritesController {

    @Autowired
    private UserFavoritesRepository repo;

    @GetMapping("/user/{userId}")
    public List<UserFavorites> getByUser(@PathVariable Integer userId) {
        return repo.findByUserUserId(userId);
    }

    @PostMapping
    public UserFavorites create(@RequestBody UserFavorites fav) {
        return repo.save(fav);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        repo.deleteById(id);
    }
}
