package com.gooddeal.service;

import java.util.Optional;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.gooddeal.model.Admins;
import com.gooddeal.repository.AdminsRepository;

@Service
public class AdminService {

    private final AdminsRepository adminsRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AdminService(AdminsRepository adminsRepository) {
        this.adminsRepository = adminsRepository;
    }

    /**
     * Admin 登入驗證
     */
    public Admins login(String username, String rawPassword) {

        Optional<Admins> optionalAdmin = adminsRepository.findByUsername(username);

        if (optionalAdmin.isEmpty()) {
            throw new RuntimeException("帳號或密碼錯誤");
        }

        Admins admin = optionalAdmin.get();

        // ⭐ 關鍵：BCrypt 比對
        boolean match = passwordEncoder.matches(
            rawPassword,
            admin.getPasswordHash()
        );

        if (!match) {
            throw new RuntimeException("帳號或密碼錯誤");
        }

        return admin;
    }

    /**
     * （選用）建立管理員（加密密碼）
     */
    public Admins createAdmin(String username, String rawPassword) {
        Admins admin = new Admins();
        admin.setUsername(username);
        admin.setPasswordHash(passwordEncoder.encode(rawPassword));
        return adminsRepository.save(admin);
    }
    
    public void createAdminIfNotExists(String username, String password) {
        if (!adminsRepository.existsByUsername(username)) {
            createAdmin(username, password);
        }
    }

}
