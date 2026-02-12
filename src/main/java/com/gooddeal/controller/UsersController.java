package com.gooddeal.controller;

import com.gooddeal.dto.ChangePasswordRequest;
import com.gooddeal.model.Users;
import com.gooddeal.repository.UsersRepository;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UsersController {

	private final UsersRepository repo; 
	private final PasswordEncoder passwordEncoder; 
	
	public UsersController(UsersRepository repo, PasswordEncoder passwordEncoder) { 
		this.repo = repo; 
		this.passwordEncoder = passwordEncoder; 
	}

    @GetMapping
    public List<Users> getAll() {
        return repo.findAll();
    }

    @GetMapping("/{id}")
    public Users getById(@PathVariable Integer id) {
        return repo.findById(id).orElse(null);
    }
    
    @GetMapping("/profile")
    public Users getProfile(HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        return repo.findById(userId).orElse(null);
    }

    @PostMapping
    public Users create(@RequestBody Users user) {
    	if (user.getPasswordHash() != null && !user.getPasswordHash().isBlank()) { 
    		user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash())); 
    	}
    	return repo.save(user);
    }

    @PutMapping("/{id}")
    public Users update(@PathVariable Integer id, @RequestBody Users newData) {
        return repo.findById(id).map(user -> {
            user.setUsername(newData.getUsername());
            user.setEmail(newData.getEmail());
            user.setRole(newData.getRole());
            user.setPasswordHash(passwordEncoder.encode(newData.getPasswordHash())); 
            return repo.save(user);
        }).orElse(null);
    }
    
    @PutMapping("/profile")
    public Users updateProfile(
            @RequestBody Users data,
            HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");

        Users user = repo.findById(userId).orElseThrow();
        user.setUsername(data.getUsername());
        user.setEmail(data.getEmail());

        return repo.save(user);
    }
    
    @PutMapping("/password")
    public Map<String, String> changePassword(
            @RequestBody ChangePasswordRequest req,
            HttpServletRequest request) {

        Integer userId = (Integer) request.getAttribute("userId");

        Users user = repo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(req.getOldPassword(), user.getPasswordHash())) {
            throw new RuntimeException("舊密碼錯誤");
        }

        user.setPasswordHash(passwordEncoder.encode(req.getNewPassword()));
        repo.save(user);

        return Map.of("message", "Password updated");
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        repo.deleteById(id);
    }
}
