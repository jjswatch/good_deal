package com.gooddeal.controller.admin;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gooddeal.model.PriceReport;
import com.gooddeal.security.AdminGuard;
import com.gooddeal.service.PriceReportService;

@RestController
@RequestMapping("/api/admin/price-reports")
public class AdminPriceReportController {

    private final PriceReportService priceReportService;

    public AdminPriceReportController(PriceReportService priceReportService) {
        this.priceReportService = priceReportService;
    }

    // 1️⃣ 取得所有待審核回報
    @GetMapping("/pending")
    public List<PriceReport> getPendingReports() {
        return priceReportService.getPendingReports();
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<?> approve(@PathVariable Integer id) {
    	AdminGuard.check();
    	priceReportService.approveReport(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<?> reject(@PathVariable Integer id) {
    	AdminGuard.check();
    	priceReportService.rejectReport(id);
        return ResponseEntity.ok().build();
    }
}

