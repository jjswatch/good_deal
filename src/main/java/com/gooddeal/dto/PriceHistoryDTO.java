package com.gooddeal.dto;

import java.time.LocalDateTime;

public class PriceHistoryDTO {
	private Integer historyId;
    private Integer productId;
    private String productName;
    private Integer storeId;
    private String storeName;
    private Integer oldPrice;
    private Integer newPrice;
    private LocalDateTime changedAt;
}
