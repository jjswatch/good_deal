package com.gooddeal.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gooddeal.model.UserPoints;
import com.gooddeal.repository.UserPointsRepository;

@RestController
@RequestMapping("/api/user-points")
public class UserPointsController {

    private final UserPointsRepository pointsRepo;

    public UserPointsController(UserPointsRepository pointsRepo) {
        this.pointsRepo = pointsRepo;
    }

    @GetMapping("/{userId}")
    public UserPoints getUserPoints(@PathVariable Integer userId) {
        // 如果找不到，回傳一個初始化的物件（避免前端噴錯）
        return pointsRepo.findById(userId).orElseGet(() -> {
            UserPoints defaultPoints = new UserPoints();
            defaultPoints.setUserId(userId);
            defaultPoints.setPoints(0);
            defaultPoints.setLevel(1);
            return defaultPoints;
        });
    }
}
