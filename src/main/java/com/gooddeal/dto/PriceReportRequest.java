package com.gooddeal.dto;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class PriceReportRequest {
	private Integer productId;
    private Integer storeId;
    private Integer userId;
    private BigDecimal reportedPrice;
}
