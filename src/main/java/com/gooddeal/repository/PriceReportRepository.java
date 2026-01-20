package com.gooddeal.repository;

import com.gooddeal.model.PriceReport;
import com.gooddeal.model.ReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PriceReportRepository extends JpaRepository<PriceReport, Integer> {

    // 前台：顯示已通過回報
    List<PriceReport> findTop5ByProductProductIdAndStatusOrderByReportedAtDesc(
            Integer productId,
            ReportStatus status
    );

    // 後台：待審核清單
    List<PriceReport> findByStatusOrderByReportedAtAsc(ReportStatus status);
}
