package com.gooddeal.controller;

import com.gooddeal.model.Categories;
import com.gooddeal.repository.CategoriesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoriesController {

    @Autowired
    private CategoriesRepository repo;

    @GetMapping
    public List<Categories> getAll() {
        return repo.findAll();
    }

    @PostMapping
    public Categories create(@RequestBody Categories category) {
        return repo.save(category);
    }

    @PutMapping("/{id}")
    public Categories update(@PathVariable Integer id, @RequestBody Categories data) {
        return repo.findById(id).map(cat -> {
            cat.setCategoryName(data.getCategoryName());
            return repo.save(cat);
        }).orElse(null);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        repo.deleteById(id);
    }
}
