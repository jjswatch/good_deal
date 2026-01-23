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
            @RequestParam List<Integer> ids
    ) {
        return service.compareBasket(ids);
    }
}
