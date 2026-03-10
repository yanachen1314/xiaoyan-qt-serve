package com.yan.xiaoyanqtserve.controller;

import com.yan.xiaoyanqtserve.common.dto.AdminUserLoginRequest;
import com.yan.xiaoyanqtserve.common.dto.AdminUserRegisterRequest;
import com.yan.xiaoyanqtserve.common.dto.AdminUserResponse;
import com.yan.xiaoyanqtserve.common.enums.Role;
import com.yan.xiaoyanqtserve.entity.AdminUser;
import com.yan.xiaoyanqtserve.security.AdminUserDetails;
import com.yan.xiaoyanqtserve.service.AdminUserService;
import com.yan.xiaoyanqtserve.util.ApiResponse;
import com.yan.xiaoyanqtserve.util.JwtUtil;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

/**
 * 公开控制器
 * @author yanchen
 * @since 2026/3/10 15:11
 */
@Slf4j
@RestController
public class PublicController {
    @Resource
    private AdminUserService adminUserService;

    @Resource
    private AuthenticationManager authenticationManager;

    @Resource
    private PasswordEncoder passwordEncoder;

    @Resource
    private JwtUtil jwtUtil;

    /**
     * 管理员登录接口
     *
     * @param loginRequest 登录请求参数
     * @return 登录结果
     */
    @PostMapping("/auth/login")
    public ApiResponse<AdminUserResponse> login(@Valid @RequestBody AdminUserLoginRequest loginRequest) {
        try {
            // 使用Spring Security进行身份验证
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()));

            // 获取用户信息
            AdminUserDetails userDetails = (AdminUserDetails) authentication.getPrincipal();
            AdminUser adminUser = adminUserService.getById(userDetails.getId());

            // 更新最后登录时间
            adminUser.setLastLoginTime(LocalDateTime.now());
            adminUserService.updateById(adminUser);

            // 生成JWT令牌
            String token = jwtUtil.generateToken(adminUser.getUsername());

            // 构造响应数据
            AdminUserResponse response = new AdminUserResponse();
            response.setId(adminUser.getId());
            response.setUsername(adminUser.getUsername());
            response.setToken(token);
            response.setRole(adminUser.getRole());
            response.setEmail(adminUser.getEmail());
            response.setPhone(adminUser.getPhone());

            return ApiResponse.success(response);
        } catch (Exception e) {
            log.error("登录失败: {}", e.getMessage());
            return ApiResponse.fail("用户名或密码错误");
        }
    }

    /**
     * 管理员注册接口
     *
     * @param registerRequest 注册请求参数
     * @return 注册结果
     */
    @PostMapping("/auth/register")
    public ApiResponse<String> register(@Valid @RequestBody AdminUserRegisterRequest registerRequest) {
        // 检查用户名是否已存在
        AdminUser existingUser = adminUserService.findByUsername(registerRequest.getUsername());

        if (existingUser != null) {
            return ApiResponse.fail("用户名已存在");
        }

        // 创建新用户
        AdminUser adminUser = new AdminUser();
        adminUser.setUsername(registerRequest.getUsername());
        adminUser.setPasswordHash(passwordEncoder.encode(registerRequest.getPassword()));
        adminUser.setRole(Role.ADMIN.name()); // 默认角色为ADMIN
        adminUser.setEmail(registerRequest.getEmail());
        adminUser.setPhone(registerRequest.getPhone());
        adminUser.setCreatedTime(LocalDateTime.now());
        adminUser.setUpdatedTime(LocalDateTime.now());

        // 保存用户
        boolean result = adminUserService.save(adminUser);
        if (result) {
            return ApiResponse.success("注册成功");
        } else {
            return ApiResponse.fail("注册失败");
        }
    }

}
