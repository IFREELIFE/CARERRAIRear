package com.endcareerai.platform.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Date;
import java.util.Map;

/**
 * JWT Token 工具类
 * 负责 JWT Token 的生成、解析和验证，使用 HMAC-SHA 签名算法
 */
@Component
public class JwtTokenProvider {

    // TODO: 请修改为你的 JWT 密钥（至少256位），建议通过环境变量注入
    @Value("${jwt.secret}")
    private String secret;

    // TODO: 请修改为你的 Token 过期时间（毫秒），当前默认24小时
    @Value("${jwt.expiration}")
    private long expiration;

    // TODO: 请根据实际安全策略调整 refresh token 有效期（毫秒），当前默认7天
    @Value("${jwt.refresh-expiration:604800000}")
    private long refreshExpiration;

    private SecretKey key;

    /**
     * 初始化 HMAC-SHA 签名密钥
     */
    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成 JWT Token
     * Token 中包含用户ID（subject）和角色（claim）
     *
     * @param userId 用户ID
     * @param role   用户角色
     * @return JWT Token 字符串
     */
    public String generateToken(Long userId, String role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("role", role)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }

    public Map<String, String> generateTokenPair(Long userId, String role) {
        Date now = new Date();
        Date accessExpiryDate = new Date(now.getTime() + expiration);
        Date refreshExpiryDate = new Date(now.getTime() + refreshExpiration);

        String accessToken = Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("role", role)
                .claim("tokenType", "access")
                .issuedAt(now)
                .expiration(accessExpiryDate)
                .signWith(key)
                .compact();

        String refreshToken = Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("role", role)
                .claim("tokenType", "refresh")
                .issuedAt(now)
                .expiration(refreshExpiryDate)
                .signWith(key)
                .compact();

        Map<String, String> tokenPair = new HashMap<>();
        tokenPair.put("accessToken", accessToken);
        tokenPair.put("refreshToken", refreshToken);
        return tokenPair;
    }

    /**
     * 从 Token 中提取用户ID
     *
     * @param token JWT Token 字符串
     * @return 用户ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return Long.parseLong(claims.getSubject());
    }

    /**
     * 从 Token 中提取角色
     *
     * @param token JWT Token 字符串
     * @return 角色字符串
     */
    public String getRoleFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.get("role", String.class);
    }

    /**
     * 验证 Token 是否有效（签名正确且未过期）
     *
     * @param token JWT Token 字符串
     * @return 是否有效
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public boolean validateRefreshToken(String token) {
        try {
            Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
            return "refresh".equals(claims.get("tokenType", String.class));
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
