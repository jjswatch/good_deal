package com.gooddeal.repository;

import com.gooddeal.model.PriceHistory;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PriceHistoryRepository extends JpaRepository<PriceHistory, Integer> {
	List<PriceHistory> findByProductProductIdOrderByChangedAtDesc(Integer productId);
	
	List<PriceHistory> findAllByOrderByChangedAtDesc();
	
	List<PriceHistory> findTop100ByOrderByChangedAtDesc();
}
