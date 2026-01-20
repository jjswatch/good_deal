package com.gooddeal.service;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gooddeal.model.PriceHistory;
import com.gooddeal.model.ProductPrices;
import com.gooddeal.repository.PriceHistoryRepository;
import com.gooddeal.repository.ProductPricesRepository;

import jakarta.transaction.Transactional;

@Service
public class ProductPriceService {

    @Autowired
    private ProductPricesRepository priceRepo;

    @Autowired
    private PriceHistoryRepository historyRepo;

    public ProductPrices getById(Integer id) {
        return priceRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("價格不存在"));
    }

    @Transactional
    public ProductPrices updatePrice(Integer priceId, BigDecimal newPrice) {

        if (newPrice == null || newPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("價格不得為負值");
        }

        ProductPrices pp = priceRepo.findById(priceId)
                .orElseThrow(() -> new RuntimeException("價格不存在"));

        BigDecimal oldPrice = pp.getPrice();

        if (oldPrice != null && oldPrice.compareTo(newPrice) == 0) {
            return pp;
        }

        pp.setPrice(newPrice);
        ProductPrices saved = priceRepo.save(pp);

        PriceHistory history = new PriceHistory();
        history.setProduct(pp.getProduct());
        history.setStore(pp.getStore());
        history.setOldPrice(oldPrice);
        history.setNewPrice(newPrice);

        historyRepo.save(history);

        return saved;
    }

}

