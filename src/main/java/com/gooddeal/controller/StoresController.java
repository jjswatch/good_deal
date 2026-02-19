package com.gooddeal.controller;

import com.gooddeal.model.Stores;
import com.gooddeal.repository.StoresRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stores")
public class StoresController {

    @Autowired
    private StoresRepository repo;

    @GetMapping
    public List<Stores> getAll() {
        return repo.findAll();
    }
    
    @GetMapping("/group/{group}")
    public List<Stores> findByStoreGroup(@PathVariable Integer group) {
    	return repo.findByStoreGroup(group);
    }

    @PostMapping
    public Stores create(@RequestBody Stores store) {
        return repo.save(store);
    }

    @PutMapping("/{id}")
    public Stores update(@PathVariable Integer id, @RequestBody Stores data) {
        return repo.findById(id).map(store -> {
            store.setStoreName(data.getStoreName());
            store.setLocation(data.getLocation());
            store.setCity(data.getCity());
            return repo.save(store);
        }).orElse(null);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        repo.deleteById(id);
    }
}
