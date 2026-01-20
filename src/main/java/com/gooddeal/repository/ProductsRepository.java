package com.gooddeal.repository;

import com.gooddeal.model.Products;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductsRepository extends JpaRepository<Products, Integer> {
	List<Products> findByProductNameContainingIgnoreCase(String keyword);
	List<Products> findByBarcode(String barcode);
	List<Products> findByCategoryCategoryId(Integer categoryId);
	List<Products> findByCategoryCategoryNameContainingIgnoreCase(String name);
	long countByCategory_CategoryId(Integer categoryId);
	List<Products> findTop5ByOrderByCreatedAtDesc();
	long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
	long countByUpdatedAtBetween(LocalDateTime start, LocalDateTime end);
	@Query("SELECT p FROM Products p WHERE " +
	           "(SELECT COUNT(s) FROM Stores s) > " +
	           "(SELECT COUNT(pp) FROM ProductPrices pp WHERE pp.product.productId = p.productId)")
	    List<Products> findProductsWithMissingPrices();
}
