package com.gooddeal.service;

import java.util.List;

import com.gooddeal.dto.PriceReportRequest;
import com.gooddeal.model.PriceReport;

public interface PriceReportService {
	PriceReport report(PriceReportRequest req);
	
	List<PriceReport> getPendingReports();

    void approveReport(Integer reportId);

    void rejectReport(Integer reportId);
}
