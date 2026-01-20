package com.gooddeal.security;

import java.io.IOException;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;

public class JwtAuthFilter implements Filter {

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest) request;
		try {
            String auth = req.getHeader("Authorization");

            if (auth != null && auth.startsWith("Bearer ")) {
                String token = auth.substring(7);

                var claims = JwtUtil.parseToken(token);

                Integer userId = claims.get("uid", Integer.class);
                String role = claims.get("role", String.class);

                AuthUtil.setAuth(userId, role);
            }

            chain.doFilter(request, response);

        } finally {
            AuthUtil.clear(); // ⭐ 防止 thread 污染（超重要）
        }
	}

}
