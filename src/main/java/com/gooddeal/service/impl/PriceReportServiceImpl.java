package com.gooddeal.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gooddeal.dto.PriceReportRequest;
import com.gooddeal.exception.ApiException;
import com.gooddeal.exception.ApiErrorCode;
import com.gooddeal.model.PriceHistory;
import com.gooddeal.model.PriceReport;
import com.gooddeal.model.ProductPrices;
import com.gooddeal.model.ReportStatus;
import com.gooddeal.model.Users;
import com.gooddeal.repository.PriceHistoryRepository;
import com.gooddeal.repository.PriceReportRepository;
import com.gooddeal.repository.ProductPricesRepository;
import com.gooddeal.repository.ProductsRepository;
import com.gooddeal.repository.StoresRepository;
import com.gooddeal.repository.UsersRepository;
import com.gooddeal.security.AuthUtil;
import com.gooddeal.service.DailyRewardService;
import com.gooddeal.service.PriceReportService;

@Service
public class PriceReportServiceImpl implements PriceReportService {

	private final PriceReportRepository reportRepo;
	private final ProductsRepository productRepo;
	private final StoresRepository storeRepo;
	private final ProductPricesRepository priceRepo;
	private final PriceHistoryRepository historyRepo;
	private final UsersRepository userRepo;
	private final DailyRewardService dailyRewardService;

	public PriceReportServiceImpl(PriceReportRepository reportRepo, ProductsRepository productRepo, StoresRepository storeRepo, ProductPricesRepository priceRepo,
			PriceHistoryRepository historyRepo, UsersRepository userRepo, DailyRewardService dailyRewardService) {
		this.reportRepo = reportRepo;
		this.productRepo = productRepo;
		this.storeRepo = storeRepo;
		this.priceRepo = priceRepo;
		this.historyRepo = historyRepo;
		this.userRepo = userRepo;
		this.dailyRewardService = dailyRewardService;
	}
	
	@Transactional
    public PriceReport report(PriceReportRequest req) {
        // 1️⃣ 基本存在檢查
        var product = productRepo.findById(req.getProductId())
                .orElseThrow(() -> new ApiException(ApiErrorCode.PRODUCT_NOT_FOUND));

        var store = storeRepo.findById(req.getStoreId())
                .orElseThrow(() -> new ApiException(ApiErrorCode.STORE_NOT_FOUND));

        var user = userRepo.findById(req.getUserId())
                .orElseThrow(() -> new ApiException(ApiErrorCode.USER_NOT_FOUND));

        // 2️⃣ 防刷：今日同商品只一次（每日任務）
        if (reportRepo.hasReportedProductToday(user.getUserId(), product.getProductId())) {
            throw new ApiException(ApiErrorCode.DUPLICATE_TODAY);
        }
        
        // 3️⃣ 防刷：同價 / 同店 / 短時間
        LocalDateTime fiveMinutesAgo = LocalDateTime.now().minusMinutes(5);

        if (reportRepo.existsSamePriceReport(
                user.getUserId(),
                product.getProductId(),
                store.getStoreId(),
                req.getReportedPrice(),
                fiveMinutesAgo
        )) {
            throw new ApiException(ApiErrorCode.SAME_PRICE_RECENT);
        }

        // 4️⃣ 防刷：短時間大量回報
        LocalDateTime tenMinutesAgo = LocalDateTime.now().minusMinutes(10);
        long recentCount = reportRepo.countRecentReports(user.getUserId(), tenMinutesAgo);

        if (recentCount >= 5) {
            throw new ApiException(ApiErrorCode.TOO_FREQUENT);
        }
        
     // 5️⃣ 建立回報
        PriceReport report = new PriceReport();
        report.setProduct(product);
        report.setStore(store);
        report.setUser(user);
        report.setReportedPrice(req.getReportedPrice());
        report.setStatus(ReportStatus.PENDING);

        return reportRepo.save(report);
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
