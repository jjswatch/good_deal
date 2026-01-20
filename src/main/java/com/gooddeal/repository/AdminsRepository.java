package com.gooddeal.repository;

import com.gooddeal.model.Admins;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminsRepository extends JpaRepository<Admins, Integer> {
	Optional<Admins> findByUsername(String username);
	boolean existsByUsername(String username);
}
