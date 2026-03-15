package com.gooddeal.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface FavoriteProjection {
	Integer getFavoriteId();
    Integer getProductId();
    String getProductName();
    String getBrand();
    String getImageUrl();
    BigDecimal getLowestPrice();
    String getBestStoreName();
    LocalDateTime getCreatedAt();
}
