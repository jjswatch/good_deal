package com.gooddeal.repository;

import com.gooddeal.model.Products;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductsRepository extends JpaRepository<Products, Integer> {
	List<Products> findByProductNameContainingIgnoreCaseOrBrandContainingIgnoreCase(
	        String productNameKeyword,
	        String brandKeyword
	);
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
	
	@Query(value = """
		    SELECT 
		        p.product_id, 
		        p.product_name, 
		        p.image_url, 
		        COUNT(DISTINCT pp.store_id) AS storeCount, 
		        COUNT(DISTINCT pr.report_id) AS reportCount, 
		        MIN(pp.price) AS minPrice
		    FROM products p
		    LEFT JOIN product_prices pp 
		        ON p.product_id = pp.product_id
		        AND pp.updated_at >= :since
		    LEFT JOIN price_reports pr 
		        ON p.product_id = pr.product_id
		        AND pr.reported_at >= :since
		    GROUP BY p.product_id
		    HAVING storeCount >= 2
		    ORDER BY reportCount DESC, storeCount DESC
		    LIMIT 6
		""", nativeQuery = true)
		List<Object[]> findHotProductsRaw(@Param("since") LocalDateTime since);
	    
		@Query(value = """
			    SELECT 
			        p.product_id, p.product_name, p.image_url, 
			        COUNT(DISTINCT pp.store_id) AS storeCount, 
			        COUNT(DISTINCT pr.report_id) AS reportCount, 
			        MIN(pp.price) AS minPrice
			    FROM products p
			    LEFT JOIN product_prices pp 
			        ON p.product_id = pp.product_id
			        AND pp.updated_at >= :since
			    LEFT JOIN price_reports pr 
			        ON p.product_id = pr.product_id
			        AND pr.reported_at >= :since
			    GROUP BY p.product_id
			    HAVING storeCount >= 1 OR reportCount >= 1
			    ORDER BY reportCount DESC, storeCount DESC
			    LIMIT 6
			""", nativeQuery = true)
			List<Object[]> findWarmProductsRaw(@Param("since") LocalDateTime since);
}
