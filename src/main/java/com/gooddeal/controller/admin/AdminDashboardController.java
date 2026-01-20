package com.gooddeal.controller.admin;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gooddeal.dto.DashboardStats;
import com.gooddeal.model.Products;
import com.gooddeal.service.AdminDashboardService;

@RestController
@RequestMapping("/api/admin/dashboard")
public class AdminDashboardController {

    private final AdminDashboardService dashboardService;

    public AdminDashboardController(AdminDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/stats")
    public DashboardStats stats() {
        return dashboardService.getStats();
    }

    @GetMapping("/latest-products")
    public List<Products> latest() {
        return dashboardService.getLatestProducts();
    }
}


