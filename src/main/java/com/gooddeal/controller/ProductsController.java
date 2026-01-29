package com.gooddeal.controller;

import com.gooddeal.model.Categories;
import com.gooddeal.model.ProductPrices;
import com.gooddeal.model.Products;
import com.gooddeal.repository.CategoriesRepository;
import com.gooddeal.repository.ProductPricesRepository;
import com.gooddeal.repository.ProductsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
public class ProductsController {

    @Autowired
    private ProductsRepository repo;

    @Autowired
    private CategoriesRepository categoryRepo;
    
    @Autowired
    private ProductPricesRepository priceRepo;

    @GetMapping
    public List<Products> getAll() {
        return repo.findAll();
    }

    @GetMapping("/{id}")
    public Products getById(@PathVariable Integer id) {
        return repo.findById(id).orElse(null);
    }

    @PostMapping
    public Products create(@RequestBody Products product) {
        return repo.save(product);
    }

    @PutMapping("/{id}")
    public Products update(@PathVariable Integer id, @RequestBody Products data) {
        return repo.findById(id).map(product -> {
            product.setProductName(data.getProductName());
            product.setBrand(data.getBrand());
            product.setSpec(data.getSpec());
            product.setBarcode(data.getBarcode());
            product.setImageUrl(data.getImageUrl());

            // 設定分類
            Categories cat = categoryRepo.findById(data.getCategory().getCategoryId()).orElse(null);
            product.setCategory(cat);

            return repo.save(product);
        }).orElse(null);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        repo.deleteById(id);
    }
    
    @GetMapping("/search")
    public List<Products> searchProducts(@RequestParam String keyword) {
    	return repo.findByProductNameContainingIgnoreCaseOrBrandContainingIgnoreCase(
    	        keyword,
    	        keyword
    	    );
    }
    
    @GetMapping("/barcode")
    public List<Products> findByBarcode(@RequestParam String code) {
        return repo.findByBarcode(code);
    }
    
    @GetMapping("/category/{categoryId}")
    public List<Products> getByCategoryId(@PathVariable Integer categoryId) {
        return repo.findByCategoryCategoryId(categoryId);
    }
    
    @GetMapping("/category")
    public List<Products> getByCategoryName(@RequestParam String name) {
        return repo.findByCategoryCategoryNameContainingIgnoreCase(name);
    }
    
    @GetMapping("/{id}/price-lowest")
    public Map<String, Object> getLowestPrices(@PathVariable Integer id) {
        // 1. 設定 30 天前的時間點
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);

        // 2. 取得數據 (Repository 已優化為回傳單一物件)
        ProductPrices recent = priceRepo.findFirstByProductProductIdAndUpdatedAtAfterOrderByPriceAscUpdatedAtDesc(id, thirtyDaysAgo);
        ProductPrices history = priceRepo.findFirstByProductProductIdOrderByPriceAscUpdatedAtDesc(id);

        // 3. 封裝結果
        Map<String, Object> response = new HashMap<>();
        
        response.put("recent30Days", recent == null ? null : Map.of(
            "price", recent.getPrice(),
            "store", recent.getStore().getStoreName(),
            "date", recent.getUpdatedAt()
        ));

        response.put("historical", history == null ? null : Map.of(
            "price", history.getPrice(),
            "store", history.getStore().getStoreName(),
            "date", history.getUpdatedAt()
        ));

        return response;
    }


}
