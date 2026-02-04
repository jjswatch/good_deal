package com.gooddeal.controller.admin;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gooddeal.dto.PriceHistoryRequest;
import com.gooddeal.dto.PriceHistoryResponse;
import com.gooddeal.service.PriceHistoryService;

@RestController
@RequestMapping("/api/admin/history")
@CrossOrigin(origins = "*")
public class AdminPriceHistoryController {
	@Autowired
    private PriceHistoryService priceHistoryService;

	// 1. GET: 取得最新的歷史價格列表 (供管理後台首頁使用)
    // 對應前端: apiGet("/admin/history/latest?limit=50")
    @GetMapping("/latest")
    public ResponseEntity<List<PriceHistoryResponse>> getAllLatestHistory(
            @RequestParam(defaultValue = "50") int limit) {
        // 在 Service 中需要實作 findAll 或 findTopN 的邏輯
        List<PriceHistoryResponse> list = priceHistoryService.getAllHistory(limit);
        return ResponseEntity.ok(list);
    }

    // 2. GET: 查詢某個商品的所有歷史價格
    @GetMapping("/product/{productId}")
    public ResponseEntity<List<PriceHistoryResponse>> getHistoryByProduct(@PathVariable Integer productId) {
        List<PriceHistoryResponse> list = priceHistoryService.getHistoryByProductId(productId);
        return ResponseEntity.ok(list);
    }

    // 3. POST: 新增一筆歷史價格
    @PostMapping
    public ResponseEntity<PriceHistoryResponse> addPriceHistory(@RequestBody PriceHistoryRequest request) {
        PriceHistoryResponse response = priceHistoryService.createPriceHistory(request);
        return ResponseEntity.ok(response);
    }

    // 4. PUT: 修改現有的歷史紀錄 (這是你缺少的更新功能)
    // URL: /api/admin/history/{historyId}
    @PutMapping("/{historyId}")
    public ResponseEntity<PriceHistoryResponse> updatePriceHistory(
            @PathVariable Integer historyId, 
            @RequestBody PriceHistoryRequest request) {
        try {
            PriceHistoryResponse response = priceHistoryService.updatePriceHistory(historyId, request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // 5. DELETE: 刪除某一筆歷史紀錄
    @DeleteMapping("/{historyId}")
    public ResponseEntity<String> deletePriceHistory(@PathVariable Integer historyId) {
        try {
            priceHistoryService.deletePriceHistory(historyId);
            return ResponseEntity.ok("Deleted successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
