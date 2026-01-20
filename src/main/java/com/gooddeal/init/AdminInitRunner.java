package com.gooddeal.init;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

import com.gooddeal.service.AdminService;

@Configuration
public class AdminInitRunner implements CommandLineRunner {
	private final AdminService adminService;

    public AdminInitRunner(AdminService adminService) {
        this.adminService = adminService;
    }

    @Override
    public void run(String... args) {
        adminService.createAdminIfNotExists("admin", "123456");
    }
}
