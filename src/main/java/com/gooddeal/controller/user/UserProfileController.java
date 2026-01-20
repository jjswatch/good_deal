package com.gooddeal.controller.user;

import java.util.Map;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gooddeal.dto.ChangePasswordRequest;
import com.gooddeal.model.Users;
import com.gooddeal.repository.UsersRepository;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/user")
public class UserProfileController {

    private final UsersRepository usersRepo;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserProfileController(UsersRepository usersRepo) {
        this.usersRepo = usersRepo;
    }

    @GetMapping("/profile")
    public Users getProfile(HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        return usersRepo.findById(userId).orElse(null);
    }
    
    @PutMapping("/profile")
    public Users updateProfile(
            @RequestBody Users data,
            HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");

        Users user = usersRepo.findById(userId).orElseThrow();
        user.setUsername(data.getUsername());
        user.setEmail(data.getEmail());

        return usersRepo.save(user);
    }
    
    @PutMapping("/password")
    public Map<String, String> changePassword(
            @RequestBody ChangePasswordRequest req,
            HttpServletRequest request) {

        Integer userId = (Integer) request.getAttribute("userId");

        Users user = usersRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(req.getOldPassword(), user.getPasswordHash())) {
            throw new RuntimeException("舊密碼錯誤");
        }

        user.setPasswordHash(passwordEncoder.encode(req.getNewPassword()));
        usersRepo.save(user);

        return Map.of("message", "Password updated");
    }

}
