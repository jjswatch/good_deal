package com.gooddeal.controller.admin;

import com.gooddeal.model.Admins;
import com.gooddeal.repository.AdminsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/admins")
public class AdminAdminsController {

    @Autowired
    private AdminsRepository repo;

    @GetMapping
    public List<Admins> getAll() {
        return repo.findAll();
    }

    @PostMapping
    public Admins create(@RequestBody Admins admin) {
        return repo.save(admin);
    }

    @PutMapping("/{id}")
    public Admins update(@PathVariable Integer id, @RequestBody Admins data) {
        return repo.findById(id).map(admin -> {
            admin.setUsername(data.getUsername());
            admin.setPasswordHash(data.getPasswordHash());
            return repo.save(admin);
        }).orElse(null);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        repo.deleteById(id);
    }
}
