package com.gooddeal.controller;

import com.gooddeal.model.ProductPrices;
import com.gooddeal.repository.ProductPricesRepository;
import com.gooddeal.service.ProductPriceService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/prices")
public class ProductPricesController {

    @Autowired
    private ProductPricesRepository repo;
    
    @Autowired
    private ProductPriceService service;

    @GetMapping
    public List<ProductPrices> getAll() {
        return repo.findAll();
    }

    @PostMapping
    public ProductPrices create(@RequestBody ProductPrices pp) {
        return repo.save(pp);
    }

    @GetMapping("/product/{productId}")
    public List<ProductPrices> getByProduct(@PathVariable Integer productId) {
        return repo.findByProductProductId(productId);
    }

    @GetMapping("/store/{storeId}")
    public List<ProductPrices> getByStore(@PathVariable Integer storeId) {
        return repo.findByStoreStoreId(storeId);
    }
    
    @GetMapping("/group/{storeGroup}")
    public List<ProductPrices> getByStoreGroup(@PathVariable Integer storeGroup) {
        return repo.findByStoreStoreGroup(storeGroup);
    }
    
    @GetMapping("/{id}")
    public ProductPrices getOne(@PathVariable Integer id) {
    	return service.getById(id);
    }
    
    @PutMapping("/{id}")
    public ProductPrices update(
            @PathVariable Integer id,
            @RequestBody Map<String, BigDecimal> body
    ) {
        return service.updatePrice(id, body.get("price"));
    }
}
