package com.gooddeal.security;

public class AdminGuard {
	public static void check() {
        if (AuthUtil.getUserId() == null) {
            throw new RuntimeException("請先登入");
        }

        if (!AuthUtil.isAdmin()) {
            throw new RuntimeException("沒有管理者權限");
        }
    }
}
