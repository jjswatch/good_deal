package com.gooddeal.controller.admin;

import com.gooddeal.model.Users;
import com.gooddeal.repository.UsersRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUsersController {

	private final UsersRepository repo; 
	private final PasswordEncoder passwordEncoder; 

	public AdminUsersController(UsersRepository repo, PasswordEncoder passwordEncoder) { 
		this.repo = repo; 
		this.passwordEncoder = passwordEncoder; 
	}

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
            if (data.getPasswordHash() != null && !data.getPasswordHash().isBlank()) {
            	user.setPasswordHash(passwordEncoder.encode(data.getPasswordHash()));
            }
            return repo.save(user);
        }).orElse(null);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        repo.deleteById(id);
    }
}
