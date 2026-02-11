package com.gooddeal.service.impl;

import com.gooddeal.model.PriceHistory;
import com.gooddeal.model.ProductPrices;
import com.gooddeal.model.Products;
import com.gooddeal.model.Stores;
import com.gooddeal.repository.PriceHistoryRepository;
import com.gooddeal.repository.ProductPricesRepository;
import com.gooddeal.repository.ProductsRepository;
import com.gooddeal.repository.StoresRepository;
import com.gooddeal.service.AdminPriceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminPriceServiceImpl implements AdminPriceService {

    private final ProductPricesRepository productPricesRepository;
    private final PriceHistoryRepository priceHistoryRepository;
    private final StoresRepository storesRepository;
    private final ProductsRepository productsRepository;

    public AdminPriceServiceImpl(
            ProductPricesRepository productPricesRepository,
            PriceHistoryRepository priceHistoryRepository,
            StoresRepository storesRepository,
            ProductsRepository productsRepository // 2. 建構子也要加入
        ) {
            this.productPricesRepository = productPricesRepository;
            this.priceHistoryRepository = priceHistoryRepository;
            this.storesRepository = storesRepository;
            this.productsRepository = productsRepository; // 3. 賦值
        }

    @Override
    public List<ProductPrices> getAllPriceRecords() {
        return productPricesRepository.findAll();
    }

    @Override
    public ProductPrices getPriceById(Integer id) {
        return productPricesRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Price record not found"));
    }

    @Override
    public ProductPrices createPriceRecord(ProductPrices record) {
        // 存主價格
        ProductPrices saved = productPricesRepository.save(record);

        // 寫入歷史
        PriceHistory history = new PriceHistory();
        history.setProduct(saved.getProduct());
        history.setStore(saved.getStore());
        history.setOldPrice(null); // ⭐ 初始價格沒有舊值
        history.setNewPrice(saved.getPrice());

        priceHistoryRepository.save(history);

        return saved;
    }

    @Override
    public ProductPrices updatePriceRecord(Integer id, ProductPrices record) {
    	ProductPrices db = getPriceById(id);
    	// 價格有變才寫歷史
        if (db.getPrice().compareTo(record.getPrice()) != 0) {

            PriceHistory history = new PriceHistory();
            history.setProduct(db.getProduct());
            history.setStore(db.getStore());
            history.setOldPrice(db.getPrice());
            history.setNewPrice(record.getPrice());

            priceHistoryRepository.save(history);
        }

        db.setStore(record.getStore());
        db.setPrice(record.getPrice());
        db.setPriceDate(record.getPriceDate());

        return productPricesRepository.save(db);
    }

    @Override
    public void deletePriceRecord(Integer id) {
        productPricesRepository.deleteById(id);
    }

	@Override
	public List<Stores> getMissingStoresByProductId(Integer productId) {
		return storesRepository.findStoresMissingPriceForProduct(productId);
	}

	@Override
	public List<Products> getAvailableProducts() {
		return productsRepository.findProductsWithMissingPrices();
	}

	@Override
	public List<Products> getMissingProductsByStoreId(Integer storeId) {
		return productsRepository.findProductsMissingPriceForStore(storeId);
	}

	@Override
	@Transactional // 確保批次操作要麼全部成功，要麼全部失敗
	public List<ProductPrices> updateBatchPriceRecords(List<ProductPrices> records) {
	    for (ProductPrices record : records) {
	        // 1. 取得舊資料比對價格
	        ProductPrices db = productPricesRepository.findById(record.getPriceId())
	                .orElseThrow(() -> new RuntimeException("找不到紀錄 ID: " + record.getPriceId()));

	        // 2. 價格有變動才寫入
	        if (db.getPrice().compareTo(record.getPrice()) != 0) {
	            // 3. 更新主表價格
	            db.setPrice(record.getPrice());
	            productPricesRepository.save(db);
	        }
	    }
	    return records;
	}
}
