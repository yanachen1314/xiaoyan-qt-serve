package com.yan.xiaoyanqtserve.controller.admin;

import com.yan.xiaoyanqtserve.common.dto.*;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * 管理员用户控制器
 * 处理管理员登录、注册、修改、删除等认证相关请求
 *
 * @author 烟尘
 * @since 2026-02-04
 */
@Slf4j
@RestController
@RequestMapping("/admin")
public class AdminUserController {

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
    @PostMapping("/login")
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
    @PostMapping("/register")
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

    /**
     * 更新管理员信息接口
     *
     * @param updateRequest 更新请求参数
     * @return 更新结果
     */
    @PostMapping("/update")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<String> update(@Valid @RequestBody AdminUserUpdateRequest updateRequest) {
        // 调用服务层方法处理更新逻辑
        boolean result = adminUserService.updateAdminUserByRequest(updateRequest);
        if (result) {
            return ApiResponse.success("更新成功");
        } else {
            // 检查是否是因为管理员不存在
            if (adminUserService.getById(updateRequest.getId()) == null) {
                return ApiResponse.fail("管理员不存在");
            }
            // 检查是否是因为用户名已存在
            if (updateRequest.getUsername() != null) {
                return ApiResponse.fail("用户名已存在");
            }
            return ApiResponse.fail("更新失败");
        }
    }

    /**
     * 删除管理员接口
     *
     * @param deleteRequest 删除请求参数
     * @return 删除结果
     */
    @PostMapping("/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<String> delete(@Valid @RequestBody AdminUserDeleteRequest deleteRequest) {
        // 检查管理员是否存在
        AdminUser existingUser = adminUserService.getById(deleteRequest.getId());
        if (existingUser == null) {
            return ApiResponse.fail("管理员不存在");
        }

        // 执行删除操作
        boolean result = adminUserService.deleteAdminUser(deleteRequest.getId());
        if (result) {
            return ApiResponse.success("删除成功");
        } else {
            return ApiResponse.fail("删除失败");
        }
    }
}
