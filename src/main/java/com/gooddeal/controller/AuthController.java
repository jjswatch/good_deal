package com.gooddeal.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gooddeal.dto.LoginRequest;
import com.gooddeal.dto.RegisterRequest;
import com.gooddeal.model.Users;
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
		userService.register(req.getUsername(), req.getEmail(), req.getPassword()); 
		return ResponseEntity.ok("註冊成功");
    }

	@PostMapping("/login") public ResponseEntity<?> login(@RequestBody LoginRequest req) { 
		Users user = userService.login(req.getUsername(), req.getPassword()); 
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
