package com.gooddeal.controller;

import com.gooddeal.dto.PriceHistoryResponse;
import com.gooddeal.model.PriceHistory;
import com.gooddeal.repository.PriceHistoryRepository;
import com.gooddeal.service.PriceHistoryService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/history")
@CrossOrigin(origins = "*")
public class PriceHistoryController {

    @Autowired
    private PriceHistoryRepository repo;
    
    @Autowired
    private PriceHistoryService priceHistoryService;

    @GetMapping
    public List<PriceHistory> getAll() {
        return repo.findAll();
    }

    @PostMapping
    public PriceHistory create(@RequestBody PriceHistory history) {
        return repo.save(history);
    }
    
    @GetMapping("/product/{productId}")
    public List<PriceHistory> getByProduct(@PathVariable Integer productId) {
        return repo.findByProductProductIdOrderByChangedAtDesc(productId);
    }
    
    @GetMapping("/discounts")
    public List<PriceHistoryResponse> getDiscountProducts() {
        return priceHistoryService.getRecentDiscounts(10);
    }
}
