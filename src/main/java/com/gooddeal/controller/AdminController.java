package com.gooddeal.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gooddeal.dto.AdminLoginRequest;
import com.gooddeal.model.Admins;
import com.gooddeal.security.JwtUtil;
import com.gooddeal.service.AdminService;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;
    
    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody AdminLoginRequest request) {
    	System.out.println("✅ AdminController login 被呼叫");
        Admins admin = adminService.login(
            request.getUsername(),
            request.getPassword()
        );
        
        String token = JwtUtil.generateToken(
        	    admin.getAdminId(),
        	    admin.getUsername(),
        	    "ADMIN"
        	);

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("adminId", admin.getAdminId());
        result.put("username", admin.getUsername());

        return result;
    }
}
