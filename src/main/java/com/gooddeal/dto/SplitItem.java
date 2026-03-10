package com.gooddeal.dto;

import java.math.BigDecimal;

import com.gooddeal.model.ProductPrices;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SplitItem {

    private Integer productId;
    private String brand;
    private String productName;
    private String storeName;
    private BigDecimal price;
    
    public SplitItem(ProductPrices p) {
        this.productId = p.getProduct().getProductId();
        this.brand = p.getProduct().getBrand();
        this.productName = p.getProduct().getProductName();
        this.storeName = p.getStore().getStoreName();
        this.price = p.getPrice();
    }
}
