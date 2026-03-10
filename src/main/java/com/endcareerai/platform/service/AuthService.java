package com.endcareerai.platform.service;

import com.endcareerai.platform.dto.request.RegisterRequest;
import com.endcareerai.platform.dto.response.LoginResponse;

public interface AuthService {
    LoginResponse register(RegisterRequest request);
}
