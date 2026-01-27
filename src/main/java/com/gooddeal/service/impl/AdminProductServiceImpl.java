package com.gooddeal.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.gooddeal.dto.ProductRequest;
import com.gooddeal.model.Categories;
import com.gooddeal.model.Products;
import com.gooddeal.repository.CategoriesRepository;
import com.gooddeal.repository.ProductsRepository;
import com.gooddeal.service.AdminProductService;

@Service
public class AdminProductServiceImpl implements AdminProductService {

    private final ProductsRepository productRepo;
    private final CategoriesRepository categoryRepo;

    public AdminProductServiceImpl(
        ProductsRepository productRepo,
        CategoriesRepository categoryRepo
    ) {
        this.productRepo = productRepo;
        this.categoryRepo = categoryRepo;
    }

    @Override
    public List<Products> getAllProducts() {
        return productRepo.findAll();
    }

    @Override
    public Products getProductById(Integer id) {
        return productRepo.findById(id)
            .orElseThrow(() -> new RuntimeException("商品不存在"));
    }

    @Override
    public Products createProduct(ProductRequest req) {
        Categories category = categoryRepo.findById(req.getCategoryId())
            .orElseThrow(() -> new RuntimeException("分類不存在"));

        Products p = new Products();
        applyRequestToProduct(p, req, category);

        return productRepo.save(p);
    }

    @Override
    public Products updateProduct(Integer id, ProductRequest req) {
        Products p = productRepo.findById(id)
            .orElseThrow(() -> new RuntimeException("商品不存在"));

        Categories category = categoryRepo.findById(req.getCategoryId())
            .orElseThrow(() -> new RuntimeException("分類不存在"));

        applyRequestToProduct(p, req, category);

        return productRepo.save(p);
    }

    @Override
    public void deleteProduct(Integer id) {
        if (!productRepo.existsById(id)) {
            throw new RuntimeException("商品不存在");
        }
        productRepo.deleteById(id);
    }

    /**
     * 共用欄位設定（避免重複）
     */
    private void applyRequestToProduct(
        Products p,
        ProductRequest req,
        Categories category
    ) {
        p.setProductName(req.getProductName());
        p.setBrand(req.getBrand());
        p.setSpec(req.getSpec());
        p.setImageUrl(req.getImageUrl());
        if (req.getBarcode() == null || req.getBarcode().trim().isEmpty()) {
            p.setBarcode(null);
        } else {
            p.setBarcode(req.getBarcode().trim());
        }
        p.setCategory(category);
    }
}
