package com.gooddeal.service;

import com.gooddeal.model.ProductPrices;
import com.gooddeal.model.Products;
import com.gooddeal.model.Stores;

import java.util.List;

public interface AdminPriceService {
    List<ProductPrices> getAllPriceRecords();
    ProductPrices getPriceById(Integer id);
    ProductPrices createPriceRecord(ProductPrices record);
    ProductPrices updatePriceRecord(Integer id, ProductPrices record);
    void deletePriceRecord(Integer id);
    List<Stores> getMissingStoresByProductId(Integer productId);
    List<Products> getAvailableProducts();
}
