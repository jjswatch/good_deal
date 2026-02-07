package com.gooddeal.repository;

import com.gooddeal.model.PriceHistory;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PriceHistoryRepository extends JpaRepository<PriceHistory, Integer> {
	List<PriceHistory> findByProductProductIdOrderByChangedAtDesc(Integer productId);
	
	List<PriceHistory> findAllByOrderByChangedAtDesc();
	
	List<PriceHistory> findTop100ByOrderByChangedAtDesc();
	
	@Query("SELECT ph FROM PriceHistory ph WHERE ph.newPrice < ph.oldPrice ORDER BY ph.changedAt DESC")
	List<PriceHistory> findRecentDiscounts(Pageable pageable);
}
