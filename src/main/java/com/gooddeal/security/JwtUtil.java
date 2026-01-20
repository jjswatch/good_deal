package com.gooddeal.security;

import java.util.Base64;
import java.util.Date;
import java.util.Map;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {
	
	private static final ObjectMapper mapper = new ObjectMapper();

    private static final SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    private static final long EXP = 1000 * 60 * 60 * 24; // 1 天

    public static String generateToken(Integer id, String username, String role) {
        return Jwts.builder()
                .setSubject(username)
                .claim("uid", id)
                .claim("role", role) // ⭐ 關鍵
                .setExpiration(new Date(System.currentTimeMillis() + EXP))
                .signWith(key)
                .compact();
    }

    public static Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    
    public static Map<String, Object> parsePayload(String token) {
        try {
            String payload = token.split("\\.")[1];
            String json = new String(Base64.getDecoder().decode(payload));
            return mapper.readValue(json, Map.class);
        } catch (Exception e) {
            throw new RuntimeException("Invalid JWT");
        }
    }
}
