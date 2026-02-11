package com.gooddeal.controller.admin;

import com.gooddeal.model.ProductPrices;
import com.gooddeal.model.Products;
import com.gooddeal.model.Stores;
import com.gooddeal.repository.ProductsRepository;
import com.gooddeal.service.AdminPriceService;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/prices")
public class AdminPricesController {

	private final AdminPriceService adminPriceService;
	
	public AdminPricesController(AdminPriceService adminPriceService, ProductsRepository productsRepository) {
        this.adminPriceService = adminPriceService;
    }

    @GetMapping
    public List<ProductPrices> getAll() {
        return adminPriceService.getAllPriceRecords();
    }

    @GetMapping("/{id}")
    public ProductPrices getById(@PathVariable Integer id) {
        return adminPriceService.getPriceById(id);
    }

    @PostMapping
    public ProductPrices create(@RequestBody ProductPrices record) {
        return adminPriceService.createPriceRecord(record);
    }

    @PutMapping("/{id}")
    public ProductPrices update(@PathVariable Integer id, @RequestBody ProductPrices record) {
        return adminPriceService.updatePriceRecord(id, record);
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Integer id) {
        adminPriceService.deletePriceRecord(id);
        return "Price record deleted";
    }
    
    @GetMapping("/missing-stores")
    public List<Stores> getMissingStores(@RequestParam Integer productId) {
        return adminPriceService.getMissingStoresByProductId(productId);
    }
    
    @GetMapping("/missing-products")
    public List<Products> getMissingProducts(@RequestParam Integer storeId) {
        return adminPriceService.getMissingProductsByStoreId(storeId);
    }
    
    @GetMapping("/available-products")
    public List<Products> getAvailableProducts() { // 括號內必須為空
        return adminPriceService.getAvailableProducts();
    }
    
    @PutMapping("/batch")
    public List<ProductPrices> updateBatch(@RequestBody List<ProductPrices> records) {
        return adminPriceService.updateBatchPriceRecords(records);
    }
}
