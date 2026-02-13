package com.gooddeal.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gooddeal.dto.LoginRequest;
import com.gooddeal.dto.RegisterRequest;
import com.gooddeal.security.JwtUtil;
import com.gooddeal.service.UserService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
	
	private final UserService userService; 
	
	public AuthController(UserService userService) { 
		this.userService = userService; 
	}
	
	@PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
		return userService.register(req.getUsername(), req.getEmail(), req.getPasswordHash()) 
			.map(user -> ResponseEntity.ok(Map.of("message", "註冊成功")))
			.orElse(ResponseEntity.badRequest().body(Map.of("error", "帳號或 Email 已被使用")));
    }

	@PostMapping("/login") 
	public ResponseEntity<?> login(@RequestBody LoginRequest req) { 
		return userService.login(req.getUsername(), req.getPassword()) 
			.map(user -> { 
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
			}) 
			.orElse(ResponseEntity.status(401).body(Map.of("error", "帳號或密碼錯誤")));
	}
}
