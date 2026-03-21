package com.endcareerai.platform.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录响应DTO，包含JWT Token、角色和用户ID
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {
    private String accessToken;
    private String refreshToken;
    private Long expiresIn;
    private String token;
    private String role;
    private Long userId;

    public LoginResponse(String accessToken, String refreshToken, Long expiresIn, String role, Long userId) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
        this.token = accessToken;
        this.role = role;
        this.userId = userId;
    }
}
