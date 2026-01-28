package com.gooddeal.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gooddeal.dto.BasketStrategyResult;
import com.gooddeal.service.BasketStrategyService;

@RestController
@RequestMapping("/api/strategy")
public class StrategyController {

    private final BasketStrategyService service;

    public StrategyController(BasketStrategyService service) {
        this.service = service;
    }

    @GetMapping("/product/{productId}")
    public Map<String, Object> getBestPriceForProduct(
            @PathVariable Integer productId,
            @RequestParam(required = false) Integer userId
    ) {
        BasketStrategyResult result =
                service.compareBasket(List.of(productId), userId);

        Map<String, Object> res = new HashMap<>();

        if (result.getSplitStrategy() != null &&
            !result.getSplitStrategy().getItems().isEmpty()) {

            var item = result.getSplitStrategy().getItems().get(0);

            res.put("price", item.getPrice());
            res.put("store", item.getStoreName());
        }

        return res;
    }
}

