package com.gooddeal.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gooddeal.repository.CategoriesRepository;
import com.gooddeal.repository.ProductsRepository;
import com.gooddeal.repository.StoresRepository;

@RestController
@RequestMapping("/api/common")
public class CommonInfoController {
	
	@Autowired
	private StoresRepository storesRepo;
	
	@Autowired
	private ProductsRepository productsRepo;
	
	@Autowired
	private CategoriesRepository categoryRepo;
	
	private static final Map<Integer, String> STORE_MAP = Map.of(
			1, "全聯",
			2, "家樂福",
			3, "愛買",
			4, "佳瑪"
	);
	
	@GetMapping("/quick-search-options")
	public Map<String, Object> getQuickSearchOptions() {
	    Map<String, Object> result = new HashMap<>();
	    
	    List<Integer> storeIds = storesRepo.findDistinctStoreGroups();
	    List<Map<String, Object>> stores = storeIds.stream()
	    		.map(id -> {
	    			Map<String, Object> store = new HashMap<>();
	    			store.put("id", id);
	    			store.put("name", STORE_MAP.getOrDefault(id, "未知商家"));
	    			return store;
	    		}).toList();
	    result.put("stores", stores);
	    
	    result.put("brands", productsRepo.findDistinctBrands());
	    
	    result.put("categories", categoryRepo.findAll());
	    
	    return result;
	}
}
