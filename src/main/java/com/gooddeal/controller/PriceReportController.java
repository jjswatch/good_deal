package com.gooddeal.controller;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gooddeal.dto.PriceReportRequest;
import com.gooddeal.dto.PriceReportResponse;
import com.gooddeal.model.PriceReport;
import com.gooddeal.model.ReportStatus;
import com.gooddeal.repository.PriceReportRepository;
import com.gooddeal.repository.ProductsRepository;
import com.gooddeal.repository.StoresRepository;
import com.gooddeal.repository.UsersRepository;
import com.gooddeal.service.PriceReportService;

@RestController
@RequestMapping("/api/price-reports")
public class PriceReportController {

    private final PriceReportRepository reportRepo;
    private final ProductsRepository productRepo;
    private final StoresRepository storeRepo;
    private final UsersRepository userRepo;
    private final PriceReportService service;

    public PriceReportController(
            PriceReportRepository reportRepo,
            ProductsRepository productRepo,
            StoresRepository storeRepo,
            UsersRepository userRepo,
            PriceReportService service
    ) {
        this.reportRepo = reportRepo;
        this.productRepo = productRepo;
        this.storeRepo = storeRepo;
        this.userRepo = userRepo;
        this.service = service;
    }
    
    @PostMapping
    public PriceReport report(@RequestBody PriceReportRequest req) {
    	return service.report(req);
    }

//    @PostMapping
//    public PriceReport reportPrice(
//            @RequestBody PriceReportRequest req
//    ) {
//        PriceReport report = new PriceReport();
//
//        report.setProduct(
//            productRepo.findById(req.getProductId()).orElseThrow()
//        );
//        report.setStore(
//            storeRepo.findById(req.getStoreId()).orElseThrow()
//        );
//
//        report.setUser(
//        	userRepo.findById(req.getUserId()).orElseThrow()
//        );
//
//        report.setReportedPrice(req.getReportedPrice());
//        report.setStatus(ReportStatus.PENDING);
//
//        return reportRepo.save(report);
//    }

    // 商品頁：顯示最近通過的回報
    @GetMapping("/product/{productId}")
    public List<PriceReportResponse> getApprovedReports(@PathVariable Integer productId) {
    	List<PriceReport> reports = reportRepo.findTop5WithDetails(productId, ReportStatus.APPROVED);

    	return reports.stream().map(r -> new PriceReportResponse(
                r.getReportId(),
                r.getStore().getStoreName(),
                r.getReportedPrice(),
                r.getUser().getUsername(),
                r.getReportedAt()
        )).limit(5).toList();
    }
}
