package com.gooddeal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DashboardStats {
	private long productCount;
    private long categoryCount;
    private long todayCreated;
    private long todayUpdated;
}
