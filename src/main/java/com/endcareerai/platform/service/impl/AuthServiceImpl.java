package com.endcareerai.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.endcareerai.platform.common.BusinessException;
import com.endcareerai.platform.common.Constants;
import com.endcareerai.platform.dto.request.ChangePasswordRequest;
import com.endcareerai.platform.dto.request.LoginRequest;
import com.endcareerai.platform.dto.request.LogoutRequest;
import com.endcareerai.platform.dto.request.RefreshRequest;
import com.endcareerai.platform.dto.request.RegisterRequest;
import com.endcareerai.platform.dto.response.LoginResponse;
import com.endcareerai.platform.entity.Enterprise;
import com.endcareerai.platform.entity.Student;
import com.endcareerai.platform.entity.User;
import com.endcareerai.platform.mapper.EnterpriseMapper;
import com.endcareerai.platform.mapper.StudentMapper;
import com.endcareerai.platform.mapper.UserMapper;
import com.endcareerai.platform.security.JwtTokenProvider;
import com.endcareerai.platform.service.AuthService;
import com.endcareerai.platform.service.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 认证服务实现类
 * 处理多平台账号注册逻辑，包含角色校验、密码加密、JWT 生成和 Redis 缓存
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final StudentMapper studentMapper;
    private final EnterpriseMapper enterpriseMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisService redisService;

    /**
     * 多平台账号注册
     * 处理流程：
     * 1. 检查邮箱唯一性
     * 2. 根据角色执行特定校验（学校需 edu 邮箱，企业需信用代码和公司名）
     * 3. 创建用户记录并加密密码
     * 4. 根据角色创建关联子表记录（学生表/企业表）
     * 5. 生成 JWT Token 并缓存用户信息到 Redis
     *
     * @param request 注册请求体
     * @return 包含 JWT Token、角色和用户ID的登录响应
     */
    @Override
    @Transactional
    public LoginResponse register(RegisterRequest request) {
        // Check if email already exists
        Long existingCount = userMapper.selectCount(
                new QueryWrapper<User>().eq("email", request.getEmail()));
        if (existingCount > 0) {
            throw new BusinessException("邮箱已被注册");
        }

        String role = request.getRole();

        // Validate role-specific requirements
        if (Constants.ROLE_SCHOOL.equals(role)) {
            String email = request.getEmail();
            if (!email.endsWith(".edu") && !email.endsWith(".edu.cn")) {
                throw new BusinessException("学校账号邮箱必须以 .edu 或 .edu.cn 结尾");
            }
        }

        if (Constants.ROLE_ENTERPRISE.equals(role)) {
            if (request.getCreditCode() == null || request.getCreditCode().isBlank()) {
                throw new BusinessException("企业注册需要统一社会信用代码");
            }
            if (request.getCompanyName() == null || request.getCompanyName().isBlank()) {
                throw new BusinessException("企业注册需要公司名称");
            }
        }

        // Create user
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);
        user.setStatus(1);
        user.setCreatedAt(LocalDateTime.now());
        userMapper.insert(user);

        // Create role-specific records
        if (Constants.ROLE_STUDENT.equals(role)) {
            Student student = new Student();
            student.setUserId(user.getId());
            studentMapper.insert(student);
        }

        if (Constants.ROLE_ENTERPRISE.equals(role)) {
            Enterprise enterprise = new Enterprise();
            enterprise.setUserId(user.getId());
            enterprise.setCompanyName(request.getCompanyName());
            enterprise.setCreditCode(request.getCreditCode());
            enterpriseMapper.insert(enterprise);
        }

        // Generate JWT token
        Map<String, String> tokenPair = jwtTokenProvider.generateTokenPair(user.getId(), role);

        // Cache user info in Redis
        redisService.set(Constants.REDIS_USER_PREFIX + user.getId(), user, 30, TimeUnit.MINUTES);
        redisService.set(Constants.REDIS_REFRESH_TOKEN_PREFIX + user.getId(), tokenPair.get("refreshToken"), 7, TimeUnit.DAYS);

        log.info("User registered: id={}, email={}, role={}", user.getId(), user.getEmail(), role);
        return new LoginResponse(tokenPair.get("accessToken"), tokenPair.get("refreshToken"), 86400000L, role, user.getId());
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("email", request.getUsername()));
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BusinessException("用户名或密码错误");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException("账号已被禁用");
        }

        Map<String, String> tokenPair = jwtTokenProvider.generateTokenPair(user.getId(), user.getRole());
        redisService.set(Constants.REDIS_USER_PREFIX + user.getId(), user, 30, TimeUnit.MINUTES);
        redisService.set(Constants.REDIS_REFRESH_TOKEN_PREFIX + user.getId(), tokenPair.get("refreshToken"), 7, TimeUnit.DAYS);
        return new LoginResponse(tokenPair.get("accessToken"), tokenPair.get("refreshToken"), 86400000L, user.getRole(), user.getId());
    }

    @Override
    public void logout(LogoutRequest request) {
        if (!jwtTokenProvider.validateRefreshToken(request.getRefreshToken())) {
            return;
        }
        Long userId = jwtTokenProvider.getUserIdFromToken(request.getRefreshToken());
        redisService.delete(Constants.REDIS_REFRESH_TOKEN_PREFIX + userId);
    }

    @Override
    public LoginResponse refresh(RefreshRequest request) {
        if (!jwtTokenProvider.validateRefreshToken(request.getRefreshToken())) {
            throw new BusinessException("刷新令牌无效或已过期");
        }
        Long userId = jwtTokenProvider.getUserIdFromToken(request.getRefreshToken());
        Object cachedToken = redisService.get(Constants.REDIS_REFRESH_TOKEN_PREFIX + userId);
        if (cachedToken == null || !request.getRefreshToken().equals(String.valueOf(cachedToken))) {
            throw new BusinessException("刷新令牌已失效，请重新登录");
        }
        User user = userMapper.selectById(userId);
        if (user == null || (user.getStatus() != null && user.getStatus() == 0)) {
            throw new BusinessException("账号不存在或已禁用");
        }

        Map<String, String> tokenPair = jwtTokenProvider.generateTokenPair(user.getId(), user.getRole());
        redisService.set(Constants.REDIS_REFRESH_TOKEN_PREFIX + user.getId(), tokenPair.get("refreshToken"), 7, TimeUnit.DAYS);
        return new LoginResponse(tokenPair.get("accessToken"), tokenPair.get("refreshToken"), 86400000L, user.getRole(), user.getId());
    }

    @Override
    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            throw new BusinessException("原密码错误");
        }
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userMapper.updateById(user);
        redisService.delete(Constants.REDIS_REFRESH_TOKEN_PREFIX + userId);
    }
}
