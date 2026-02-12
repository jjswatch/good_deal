package com.gooddeal.init;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

import com.gooddeal.config.AdminProperties;
import com.gooddeal.service.UserService;

@Configuration
public class AdminInitRunner implements CommandLineRunner {
	
	private final UserService userService;
	private final AdminProperties adminProperties;

	public AdminInitRunner(UserService userService, AdminProperties adminProperties) { 
		this.userService = userService; 
		this.adminProperties = adminProperties; 
	}

    @Override
    public void run(String... args) {
    	userService.createAdminIfNotExists(
    			adminProperties.getUsername(), adminProperties.getPassword(), adminProperties.getEmail()
    	);
    	System.out.println("✅ 初始管理員檢查完成，帳號：" + adminProperties.getUsername());
    }
}
