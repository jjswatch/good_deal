package com.gooddeal.repository;

import com.gooddeal.model.Users;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UsersRepository extends JpaRepository<Users, Integer> {
	Users findByUsername(String username);
    Users findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
}
