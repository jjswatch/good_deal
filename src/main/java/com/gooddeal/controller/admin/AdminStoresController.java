package com.gooddeal.controller.admin;

import com.gooddeal.model.Stores;
import com.gooddeal.service.AdminStoreService;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/stores")
public class AdminStoresController {

	private final AdminStoreService adminStoreService;

    public AdminStoresController(AdminStoreService adminStoreService) {
        this.adminStoreService = adminStoreService;
    }

    @GetMapping
    public List<Stores> getAll() {
        return adminStoreService.getAllStores();
    }

    @GetMapping("/{id}")
    public Stores getById(@PathVariable Integer id) {
        return adminStoreService.getStoreById(id);
    }

    @PostMapping
    public Stores create(@RequestBody Stores store) {
        return adminStoreService.createStore(store);
    }

    @PutMapping("/{id}")
    public Stores update(@PathVariable Integer id, @RequestBody Stores store) {
        return adminStoreService.updateStore(id, store);
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Integer id) {
        adminStoreService.deleteStore(id);
        return "Store deleted";
    }
}
