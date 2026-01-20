package com.gooddeal.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.gooddeal.model.Users;
import com.gooddeal.repository.UsersRepository;

@Service
public class UserService {

    private final UsersRepository usersRepo;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public UserService(UsersRepository usersRepo) {
        this.usersRepo = usersRepo;
    }

    public void changePassword(Integer userId, String oldPwd, String newPwd) {

        Users user = usersRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("使用者不存在"));

        // 1️⃣ 驗證舊密碼
        if (!encoder.matches(oldPwd, user.getPasswordHash())) {
            throw new RuntimeException("舊密碼錯誤");
        }

        // 2️⃣ 新密碼加密
        user.setPasswordHash(encoder.encode(newPwd));
        usersRepo.save(user);
    }
}
