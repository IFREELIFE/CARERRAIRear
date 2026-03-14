package com.endcareerai.platform.service;

import com.endcareerai.platform.dto.request.RegisterRequest;
import com.endcareerai.platform.dto.response.LoginResponse;

/**
 * 认证服务接口
 * 处理用户注册、角色校验及 JWT Token 生成
 */
public interface AuthService {

    /**
     * 多平台账号注册
     * 根据角色执行不同校验逻辑：学生直接注册，学校需 edu 邮箱验证，企业需三要素验证
     *
     * @param request 注册请求
     * @return 包含 JWT Token、角色和用户ID的登录响应
     */
    LoginResponse register(RegisterRequest request);
}
