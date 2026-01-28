package com.gooddeal.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gooddeal.dto.BasketStrategyResult;
import com.gooddeal.service.BasketStrategyService;

@RestController
@RequestMapping("/api/strategies")
public class BasketStrategyController {

    @Autowired
    private BasketStrategyService service;

    @GetMapping("/compare-basket")
    public BasketStrategyResult compareBasket(
            @RequestParam List<Integer> ids,
            @RequestParam(required = false) Integer userId // 新增 userId 參數
    ) {
        // 在實務中，userId 應該從 Spring Security 的 Context 中取得，
        // 這裡為了方便 demo 先用 RequestParam
        return service.compareBasket(ids, userId);
    }
}
