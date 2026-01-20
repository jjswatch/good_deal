package com.gooddeal.security;

public class AuthUtil {
	
	private static final ThreadLocal<Integer> userIdHolder = new ThreadLocal<>();
    private static final ThreadLocal<String> roleHolder = new ThreadLocal<>();
    
    public static void setAuth(Integer userId, String role) {
        userIdHolder.set(userId);
        roleHolder.set(role);
    }
    
	public static Integer getUserId() {
		return userIdHolder.get();
    }

    public static String getRole() {
    	return roleHolder.get();
    }

    public static boolean isAdmin() {
        return "ADMIN".equals(roleHolder.get());
    }

    public static void clear() {
        userIdHolder.remove();
        roleHolder.remove();
    }
}
