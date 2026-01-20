package com.gooddeal.service;

import java.util.List;

import com.gooddeal.model.PriceReport;

public interface PriceReportService {
	List<PriceReport> getPendingReports();

    void approveReport(Integer reportId);

    void rejectReport(Integer reportId);
}
