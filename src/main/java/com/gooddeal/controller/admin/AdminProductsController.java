package com.gooddeal.controller.admin;

import com.gooddeal.dto.ProductRequest;
import com.gooddeal.model.Products;
import com.gooddeal.service.AdminProductService;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/products")
public class AdminProductsController {

	private final AdminProductService adminProductService;

    public AdminProductsController(AdminProductService adminProductService) {
        this.adminProductService = adminProductService;
    }

    @GetMapping
    public List<Products> getAll() {
        return adminProductService.getAllProducts();
    }

    @GetMapping("/{id}")
    public Products getById(@PathVariable Integer id) {
        return adminProductService.getProductById(id);
    }

    @PostMapping
    public Products create(@RequestBody ProductRequest req) {
        return adminProductService.createProduct(req);
    }

    @PutMapping("/{id}")
    public Products update(@PathVariable Integer id, @RequestBody ProductRequest req
    ) {
        return adminProductService.updateProduct(id, req);
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Integer id) {
        adminProductService.deleteProduct(id);
        return "Product deleted";
    }
}
