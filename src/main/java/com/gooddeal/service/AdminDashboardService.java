package com.gooddeal.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.gooddeal.dto.DashboardStats;
import com.gooddeal.model.Products;
import com.gooddeal.repository.CategoriesRepository;
import com.gooddeal.repository.ProductsRepository;

@Service
public class AdminDashboardService {

    private final ProductsRepository productsRepo;
    private final CategoriesRepository categoriesRepo;

    public AdminDashboardService(
        ProductsRepository productsRepo,
        CategoriesRepository categoriesRepo
    ) {
        this.productsRepo = productsRepo;
        this.categoriesRepo = categoriesRepo;
    }

    public DashboardStats getStats() {
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = LocalDateTime.now();

        return new DashboardStats(
            productsRepo.count(),
            categoriesRepo.count(),
            productsRepo.countByCreatedAtBetween(start, end),
            productsRepo.countByUpdatedAtBetween(start, end)
        );
    }

    public List<Products> getLatestProducts() {
        return productsRepo.findTop5ByOrderByCreatedAtDesc();
    }
}

