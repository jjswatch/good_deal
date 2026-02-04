package com.gooddeal.repository;

import com.gooddeal.model.PriceReport;
import com.gooddeal.model.ReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PriceReportRepository extends JpaRepository<PriceReport, Integer> {

    // 前台：顯示已通過回報
    List<PriceReport> findTop5ByProductProductIdAndStatusOrderByReportedAtDesc(
            Integer productId,
            ReportStatus status
    );

    // 後台：待審核清單
    List<PriceReport> findByStatusOrderByReportedAtAsc(ReportStatus status);
    
    @Query("SELECT r FROM PriceReport r " +
            "JOIN FETCH r.store " +
            "JOIN FETCH r.user " +
            "WHERE r.product.productId = :productId AND r.status = :status " +
            "ORDER BY r.reportedAt DESC")
     List<PriceReport> findTop5WithDetails(
             @Param("productId") Integer productId, 
             @Param("status") ReportStatus status);
    
    @Query("SELECT r FROM PriceReport r " +
    	       "JOIN FETCH r.product " +
    	       "JOIN FETCH r.store " +
    	       "JOIN FETCH r.user " +
    	       "WHERE r.status = :status " +
    	       "ORDER BY r.reportedAt ASC")
    	List<PriceReport> findAllPendingWithDetails(@Param("status") ReportStatus status);
    
    
}
