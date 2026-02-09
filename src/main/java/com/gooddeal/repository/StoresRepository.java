package com.gooddeal.repository;

import com.gooddeal.model.Stores;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StoresRepository extends JpaRepository<Stores, Integer> {
	List<Stores> findByStoreGroup(Integer group);
	
	@Query("SELECT s FROM Stores s WHERE s.storeId NOT IN " +
	           "(SELECT pp.store.storeId FROM ProductPrices pp WHERE pp.product.productId = :productId)")
	List<Stores> findStoresMissingPriceForProduct(@Param("productId") Integer productId);
	
	@Query("SELECT DISTINCT s.storeGroup FROM Stores s WHERE s.storeGroup IS NOT NULL")
	List<Integer> findDistinctStoreGroups();
}
