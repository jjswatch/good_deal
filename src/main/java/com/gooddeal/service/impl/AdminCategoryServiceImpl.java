package com.gooddeal.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.gooddeal.model.Categories;
import com.gooddeal.repository.CategoriesRepository;
import com.gooddeal.repository.ProductsRepository;
import com.gooddeal.service.AdminCategoryService;

@Service
public class AdminCategoryServiceImpl implements AdminCategoryService {

    private final CategoriesRepository repo;
    private final ProductsRepository productsRepo;

    public AdminCategoryServiceImpl(
        CategoriesRepository repo,
        ProductsRepository productsRepo
    ) {
        this.repo = repo;
        this.productsRepo = productsRepo;
    }

    @Override
    public List<Categories> findAll() {
        return repo.findAll();
    }

    @Override
    public Categories create(Categories category) {
        return repo.save(category);
    }

    @Override
    public Categories update(Integer id, Categories category) {
        Categories c = repo.findById(id)
            .orElseThrow(() -> new RuntimeException("分類不存在"));

        c.setCategoryName(category.getCategoryName());
        return repo.save(c);
    }

    @Override
    public void delete(Integer id) {
        long count = productsRepo.countByCategory_CategoryId(id);
        if (count > 0) {
            throw new RuntimeException("分類底下還有商品，無法刪除");
        }
        repo.deleteById(id);
    }
}
