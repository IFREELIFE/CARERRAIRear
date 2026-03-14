package com.endcareerai.platform.controller;

import com.endcareerai.platform.common.Result;
import com.endcareerai.platform.dto.request.RegisterRequest;
import com.endcareerai.platform.dto.response.LoginResponse;
import com.endcareerai.platform.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证与基础信息控制器（模块一）
 * 处理多平台账号注册，支持学生、学校、企业三种角色
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 多平台账号注册接口
     * 学生需提供邮箱和基本信息；学校需edu邮箱后缀验证；企业需三要素验证（公司名、信用代码、法人）
     * 注册成功后自动返回 JWT Token 和角色信息
     *
     * @param request 注册请求体，包含角色、邮箱、密码及角色特定字段
     * @return 包含 JWT Token、角色和用户ID的登录响应
     */
    @PostMapping("/register")
    public Result<LoginResponse> register(@RequestBody @Valid RegisterRequest request) {
        LoginResponse loginResponse = authService.register(request);
        return Result.success(loginResponse);
    }
}
