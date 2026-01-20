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
    public void run(String... args) throws Exception {

        try {
            adminService.createAdmin("admin", "123456");
            System.out.println("✅ 預設管理員建立完成");
        } catch (Exception e) {
            System.out.println("ℹ️ 管理員已存在，略過建立");
        }
    }
}
