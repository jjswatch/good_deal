package com.gooddeal.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gooddeal.model.UserPoints;

public interface UserPointsRepository extends JpaRepository<UserPoints, Integer> {
}
