package com.gooddeal.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.gooddeal.model.PriceHistory;
import com.gooddeal.model.PriceReport;
import com.gooddeal.model.ProductPrices;
import com.gooddeal.model.ReportStatus;
import com.gooddeal.model.Users;
import com.gooddeal.repository.PriceHistoryRepository;
import com.gooddeal.repository.PriceReportRepository;
import com.gooddeal.repository.ProductPricesRepository;
import com.gooddeal.repository.UsersRepository;
import com.gooddeal.security.AuthUtil;
import com.gooddeal.service.DailyRewardService;
import com.gooddeal.service.PriceReportService;

@Service
public class PriceReportServiceImpl implements PriceReportService {

	private final PriceReportRepository reportRepo;
	private final ProductPricesRepository priceRepo;
	private final PriceHistoryRepository historyRepo;
	private final UsersRepository userRepo;
	private final DailyRewardService dailyRewardService;

	public PriceReportServiceImpl(PriceReportRepository reportRepo, ProductPricesRepository priceRepo,
			PriceHistoryRepository historyRepo, UsersRepository userRepo, DailyRewardService dailyRewardService) {
		this.reportRepo = reportRepo;
		this.priceRepo = priceRepo;
		this.historyRepo = historyRepo;
		this.userRepo = userRepo;
		this.dailyRewardService = dailyRewardService;
	}

	@Override
	public List<PriceReport> getPendingReports() {
		return reportRepo.findAllPendingWithDetails(ReportStatus.PENDING);
	}

	@Override
	public void approveReport(Integer reportId) {
		PriceReport report = reportRepo.findById(reportId).orElseThrow(() -> new RuntimeException("Report not found"));

		if (!"ADMIN".equals(AuthUtil.getRole())) {
			throw new RuntimeException("No permission");
		}

		Users admin = userRepo.findById(AuthUtil.getUserId())
				.orElseThrow(() -> new RuntimeException("Admin not found"));

		// 1️⃣ 找目前商品 + 店家的最新價格
		ProductPrices latest = priceRepo.findTopByProductAndStoreOrderByPriceDateDesc(report.getProduct(),
				report.getStore());

		// 2️⃣ 若已有價格 → 寫入歷史
		if (latest != null) {
			PriceHistory history = new PriceHistory();
			history.setProduct(report.getProduct());
			history.setStore(report.getStore());
			history.setOldPrice(latest.getPrice());
			history.setNewPrice(report.getReportedPrice());
			historyRepo.save(history);

			// 更新價格
			latest.setPrice(report.getReportedPrice());
			latest.setPriceDate(LocalDate.now());
			priceRepo.save(latest);
		} else {
			// 3️⃣ 若沒有 → 新增一筆價格 → 寫入歷史
			ProductPrices newPrice = new ProductPrices();
			newPrice.setProduct(report.getProduct());
			newPrice.setStore(report.getStore());
			newPrice.setPrice(report.getReportedPrice());
			newPrice.setPriceDate(LocalDate.now());
			priceRepo.save(newPrice);

			PriceHistory history = new PriceHistory();
			history.setProduct(report.getProduct());
			history.setStore(report.getStore());
			history.setNewPrice(report.getReportedPrice());
			historyRepo.save(history);
		}

		Integer userId = report.getUser().getUserId();
		Integer productId = report.getProduct().getProductId();

		LocalDate today = LocalDate.now();
		LocalDateTime start = today.atStartOfDay();
		LocalDateTime end = start.plusDays(1);

		boolean isFirstProductToday = !reportRepo.existsApprovedTodayByUserAndProduct(userId, productId, start, end);

		// 每日回報商品獎勵
		dailyRewardService.handleApprovedReport(report.getUser().getUserId(), report.getProduct().getProductId(), isFirstProductToday);

		// 4️⃣ 更新回報狀態
		report.setStatus(ReportStatus.APPROVED);
		report.setApprovedBy(admin);
		report.setApprovedAt(LocalDateTime.now());
		reportRepo.save(report);
	}

	@Override
	public void rejectReport(Integer reportId) {
		PriceReport report = reportRepo.findById(reportId).orElseThrow(() -> new RuntimeException("Report not found"));

		report.setStatus(ReportStatus.REJECTED);
		reportRepo.save(report);
	}
}
