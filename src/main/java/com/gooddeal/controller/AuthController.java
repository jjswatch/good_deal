package com.gooddeal.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gooddeal.dto.LoginRequest;
import com.gooddeal.dto.RegisterRequest;
import com.gooddeal.model.UserRole;
import com.gooddeal.model.Users;
import com.gooddeal.repository.UsersRepository;
import com.gooddeal.security.JwtUtil;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
	@Autowired
	private UsersRepository usersRepo;
	
	private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
	
	@PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {

        if (usersRepo.existsByEmail(req.getEmail())) {
            throw new RuntimeException("Email 已被註冊");
        }
        
        if (usersRepo.existsByUsername(req.getUsername())) {
            return ResponseEntity.badRequest().body("帳號已被使用");
        }

        Users user = new Users();
        user.setUsername(req.getUsername());
        user.setEmail(req.getEmail());
        user.setPasswordHash(encoder.encode(req.getPassword()));
        user.setRole(UserRole.USER);

        usersRepo.save(user);

        return ResponseEntity.ok("註冊成功");
    }

	@PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {

        Users user = usersRepo.findByUsername(req.getUsername());

        if (user == null) {
            user = usersRepo.findByEmail(req.getUsername());
        }

        if (user == null || !encoder.matches(req.getPassword(), user.getPasswordHash())) {
            return ResponseEntity.status(401).body("帳號或密碼錯誤");
        }

        String token = JwtUtil.generateToken(
        	    user.getUserId(),
        	    user.getUsername(),
        	    user.getRole().name()
        	);

        return ResponseEntity.ok(Map.of(
            "token", token,
            "user", Map.of(
            	    "id", user.getUserId(),
            	    "username", user.getUsername(),
            	    "email", user.getEmail(),
            	    "role", user.getRole().name()
            	)
        ));
    }
}
