package com.endcareerai.platform.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 注册请求DTO，包含角色、邮箱、密码及企业专有字段
 */
@Data
public class RegisterRequest {
    @NotBlank
    private String role;     // STUDENT | SCHOOL | ENTERPRISE

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String password;

    private String creditCode;    // enterprise only

    private String companyName;   // enterprise only
}
