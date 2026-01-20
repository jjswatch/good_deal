package com.gooddeal.controller.admin;

import com.gooddeal.model.Categories;
import com.gooddeal.service.AdminCategoryService;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/categories")
public class AdminCategoriesController {

    private final AdminCategoryService service;

    public AdminCategoriesController(AdminCategoryService service) {
        this.service = service;
    }

    @GetMapping
    public List<Categories> list() {
        return service.findAll();
    }

    @PostMapping
    public Categories create(@RequestBody Categories c) {
        return service.create(c);
    }

    @PutMapping("/{id}")
    public Categories update(@PathVariable Integer id,
                             @RequestBody Categories c) {
        return service.update(id, c);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        service.delete(id);
    }
}

