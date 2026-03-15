package com.gooddeal.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class FavoriteResponseDTO {
		Integer favoriteId;
	    Integer productId;
	    String productName;
	    String brand;
	    String imageUrl;
	    BigDecimal lowestPrice;
	    String bestStoreName;
	    LocalDateTime addedAt;
}

