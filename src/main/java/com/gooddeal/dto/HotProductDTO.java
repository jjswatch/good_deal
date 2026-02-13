package com.gooddeal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HotProductDTO {

    private Integer productId;
    private String productName;
    private String brand;
    private String spec;
    private String imageUrl;
    private Long storeCount;
    private Long reportCount;
    private Integer minPrice;
}
