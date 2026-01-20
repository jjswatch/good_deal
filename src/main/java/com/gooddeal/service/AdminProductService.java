package com.gooddeal.service;

import java.util.List;

import com.gooddeal.dto.ProductRequest;
import com.gooddeal.model.Products;

public interface AdminProductService {

    List<Products> getAllProducts();

    Products getProductById(Integer id);

    Products createProduct(ProductRequest req);

    Products updateProduct(Integer id, ProductRequest req);

    void deleteProduct(Integer id);
}
