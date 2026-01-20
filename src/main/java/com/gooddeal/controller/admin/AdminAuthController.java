package com.gooddeal.controller.admin;

import com.gooddeal.model.Admins;
import com.gooddeal.repository.AdminsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/auth")
public class AdminAuthController {

    @Autowired
    private AdminsRepository adminRepo;

    @PostMapping("/login")
    public Object login(@RequestBody Admins loginData) {
        Admins admin = adminRepo.findAll().stream()
                .filter(a -> a.getUsername().equals(loginData.getUsername())
                        && a.getPasswordHash().equals(loginData.getPasswordHash()))
                .findFirst()
                .orElse(null);

        if (admin == null) {
            return "Login failed: invalid username or password";
        }

        return admin;  // 之後可改成 JWT Token
    }
}
