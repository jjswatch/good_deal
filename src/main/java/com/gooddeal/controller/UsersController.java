package com.gooddeal.controller;

import com.gooddeal.model.Users;
import com.gooddeal.repository.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UsersController {

    @Autowired
    private UsersRepository repo;

    @GetMapping
    public List<Users> getAll() {
        return repo.findAll();
    }

    @GetMapping("/{id}")
    public Users getById(@PathVariable Integer id) {
        return repo.findById(id).orElse(null);
    }

    @PostMapping
    public Users create(@RequestBody Users user) {
        return repo.save(user);
    }

    @PutMapping("/{id}")
    public Users update(@PathVariable Integer id, @RequestBody Users newData) {
        return repo.findById(id).map(user -> {
            user.setUsername(newData.getUsername());
            user.setEmail(newData.getEmail());
            user.setPasswordHash(newData.getPasswordHash());
            return repo.save(user);
        }).orElse(null);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        repo.deleteById(id);
    }
}
