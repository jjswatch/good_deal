package com.gooddeal.service;

import java.util.List;

import com.gooddeal.model.Categories;

public interface AdminCategoryService {
	List<Categories> findAll();
    Categories create(Categories category);
    Categories update(Integer id, Categories category);
    void delete(Integer id);
}
