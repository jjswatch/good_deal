package com.gooddeal.controller;

import com.gooddeal.model.PriceHistory;
import com.gooddeal.repository.PriceHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/history")
@CrossOrigin(origins = "*")
public class PriceHistoryController {

    @Autowired
    private PriceHistoryRepository repo;

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
}
