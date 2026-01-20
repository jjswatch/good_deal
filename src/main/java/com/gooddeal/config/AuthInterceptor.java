package com.gooddeal.config;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.gooddeal.security.JwtUtil;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest req,
                             HttpServletResponse res,
                             Object handler) throws Exception {

        String path = req.getRequestURI();

        // ===============================
        // 1️⃣ 登入 API → 永遠放行
        // ===============================
        if (path.startsWith("/api/auth")
         || path.startsWith("/api/admin/login")) {
            return true;
        }

        // ===============================
        // 2️⃣ 檢查 JWT
        // ===============================
        String auth = req.getHeader(HttpHeaders.AUTHORIZATION);
        if (auth == null || !auth.startsWith("Bearer ")) {
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        Claims claims;
        try {
            claims = JwtUtil.parseToken(auth.substring(7));
        } catch (Exception e) {
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        Integer userId = claims.get("uid", Integer.class);
        String role = claims.get("role", String.class);

        // ===============================
        // 3️⃣ Admin API → ADMIN only
        // ===============================
        if (path.startsWith("/api/admin") && !"ADMIN".equals(role)) {
            res.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }

        // ===============================
        // 4️⃣ User API → USER or ADMIN
        // ===============================
        if (path.startsWith("/api/user")
                && !("USER".equals(role) || "ADMIN".equals(role))) {
                   res.setStatus(HttpServletResponse.SC_FORBIDDEN);
                   return false;
               }

        // 可選：存使用者資訊給 Controller 用
        req.setAttribute("userId", userId);
        req.setAttribute("role", role);

        return true;
    }
}

