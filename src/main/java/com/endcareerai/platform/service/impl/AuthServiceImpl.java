package com.endcareerai.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.endcareerai.platform.common.BusinessException;
import com.endcareerai.platform.common.Constants;
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
import java.util.concurrent.TimeUnit;

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
        String token = jwtTokenProvider.generateToken(user.getId(), role);

        // Cache user info in Redis
        redisService.set(Constants.REDIS_USER_PREFIX + user.getId(), user, 30, TimeUnit.MINUTES);

        log.info("User registered: id={}, email={}, role={}", user.getId(), user.getEmail(), role);
        return new LoginResponse(token, role, user.getId());
    }
}
