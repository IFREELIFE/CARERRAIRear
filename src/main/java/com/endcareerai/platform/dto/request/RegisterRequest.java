package com.endcareerai.platform.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

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
