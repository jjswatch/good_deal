package com.gooddeal.controller.admin;

import com.gooddeal.model.Users;
import com.gooddeal.repository.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUsersController {

    @Autowired
    private UsersRepository repo;

    @GetMapping
    public List<Users> getAll() {
        return repo.findAll();
    }

    @GetMapping("/{id}")
    public Users getOne(@PathVariable Integer id) {
        return repo.findById(id).orElse(null);
    }

    @PutMapping("/{id}")
    public Users update(@PathVariable Integer id, @RequestBody Users data) {
        return repo.findById(id).map(user -> {
            user.setUsername(data.getUsername());
            user.setEmail(data.getEmail());
            if (data.getRole() != null) {
                user.setRole(data.getRole());
            }
            if (data.getPasswordHash() != null && !data.getPasswordHash().isEmpty()) {
                user.setPasswordHash(data.getPasswordHash());
            }
            return repo.save(user);
        }).orElse(null);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        repo.deleteById(id);
    }
}
