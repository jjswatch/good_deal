package com.gooddeal.service;

import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.gooddeal.model.UserRole;
import com.gooddeal.model.Users;
import com.gooddeal.repository.UsersRepository;

@Service
public class UserService {

	private final UsersRepository usersRepo; 
	private final PasswordEncoder encoder; 
	
	public UserService(UsersRepository usersRepo, PasswordEncoder encoder) { 
		this.usersRepo = usersRepo; 
		this.encoder = encoder; 
	}

	public Optional<Users> register(String username, String email, String rawPassword) { 
		if (usersRepo.existsByEmail(email)) { 
			return Optional.empty(); 
		}
		if (usersRepo.existsByUsername(username)) { 
			return Optional.empty();
		} 
		Users user = new Users(); 
		user.setUsername(username); 
		user.setEmail(email); 
		user.setPasswordHash(encoder.encode(rawPassword)); 
		user.setRole(UserRole.USER); 
		return Optional.of(usersRepo.save(user));
	}
	
	public Optional<Users> login(String usernameOrEmail, String rawPassword) { 
		Users user = usersRepo.findByUsername(usernameOrEmail); 
		if (user == null) { 
			user = usersRepo.findByEmail(usernameOrEmail); 
		} 
		if (user == null || !encoder.matches(rawPassword, user.getPasswordHash())) { 
			return Optional.empty();
		} 
		return Optional.of(user);
	}
	
	public void changePassword(Integer userId, String oldPwd, String newPwd) { 
		Users user = usersRepo.findById(userId) .orElseThrow(() -> new RuntimeException("使用者不存在")); 
		if (!encoder.matches(oldPwd, user.getPasswordHash())) { 
			throw new RuntimeException("舊密碼錯誤"); 
		} 
		user.setPasswordHash(encoder.encode(newPwd)); 
		usersRepo.save(user); 
	}
	
	public Users createAdmin(String username, String rawPassword, String email) { 
		Users admin = new Users(); 
		admin.setUsername(username); 
		admin.setPasswordHash(encoder.encode(rawPassword)); 
		admin.setEmail(email); admin.setRole(UserRole.ADMIN); 
		return usersRepo.save(admin); 
	}
	
	public void createAdminIfNotExists(String username, String password, String email) { 
		if (!usersRepo.existsByUsername(username)) { 
			createAdmin(username, password, email); 
		} 
	}
}
