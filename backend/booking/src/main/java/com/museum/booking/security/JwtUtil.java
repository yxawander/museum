package com.museum.booking.security;

import com.museum.booking.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

/**
 * JWT 生成与解析工具。
 */
@Component
public class JwtUtil {

    private final SecretKey secretKey;
    private final long expireMillis;

    public JwtUtil(JwtProperties properties) {
        this.secretKey = Keys.hmacShaKeyFor(properties.getSecret().getBytes(StandardCharsets.UTF_8));
        this.expireMillis = properties.getExpireHours() * 3600_000L;
    }

    public String generate(Long userId, String username, String role) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("username", username)
                .claim("role", role)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(expireMillis)))
                .signWith(secretKey)
                .compact();
    }

    public Claims parse(String token) {
        try {
            return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
        } catch (JwtException | IllegalArgumentException e) {
            throw new com.museum.booking.common.BusinessException(401, "登录状态已失效，请重新登录");
        }
    }
}