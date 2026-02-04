package com.yan.xiaoyanqtserve.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yan.xiaoyanqtserve.common.dto.*;
import com.yan.xiaoyanqtserve.common.enums.BizStatus;
import com.yan.xiaoyanqtserve.entity.AdminUser;
import com.yan.xiaoyanqtserve.service.AdminUserService;
import com.yan.xiaoyanqtserve.util.ApiResponse;
import com.yan.xiaoyanqtserve.util.JwtUtil;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
public class AdminUserController2 {

    @Resource
    private AdminUserService adminUserService;

    @Resource
    private JwtUtil jwtUtil;

    @Resource
    private AuthenticationManager authenticationManager;

    @Resource
    private PasswordEncoder passwordEncoder;

    /**
     * 管理员注册接口
     * <p>
     * 注册新的管理员用户，默认角色为 ADMIN
     *
     * @param request 注册请求参数，包含用户名、密码、邮箱和电话
     * @return 注册成功后返回用户信息和JWT令牌
     */
    @PostMapping("/register")
    public ApiResponse<AdminUserResponse> register(@Valid @RequestBody AdminUserRegisterRequest request) {
        log.info("管理员注册请求: {}", request.getUsername());

        try {
            // 1. 检查用户名是否已存在
            AdminUser existingUser = adminUserService.getOne(
                    new LambdaQueryWrapper<AdminUser>()
                            .eq(AdminUser::getUsername, request.getUsername()));
            if (existingUser != null) {
                log.warn("用户名已存在: {}", request.getUsername());
                return ApiResponse.fail(BizStatus.CONFLICT, "用户名已存在");
            }

            // 2. 密码加密处理
            String encodedPassword = passwordEncoder.encode(request.getPassword());

            // 3. 创建管理员用户对象
            AdminUser adminUser = new AdminUser();
            adminUser.setUsername(request.getUsername());
            adminUser.setPasswordHash(encodedPassword);
            adminUser.setRole("ADMIN"); // 默认角色为 ADMIN
            adminUser.setEmail(request.getEmail());
            adminUser.setPhone(request.getPhone());
            adminUser.setCreatedTime(LocalDateTime.now());

            // 4. 保存用户到数据库
            boolean saveResult = adminUserService.save(adminUser);
            if (!saveResult) {
                log.error("管理员注册失败: {}", request.getUsername());
                return ApiResponse.fail(BizStatus.ERROR, "注册失败，请稍后重试");
            }

            // 5. 生成 JWT 令牌
            String token = jwtUtil.generateToken(adminUser.getUsername());

            // 6. 构建响应对象
            AdminUserResponse response = buildAdminUserResponse(adminUser, token);

            log.info("管理员注册成功: {}", request.getUsername());
            return ApiResponse.success("注册成功", response);
        } catch (Exception e) {
            log.error("管理员注册异常: {}", e.getMessage(), e);
            return ApiResponse.fail(BizStatus.ERROR, "系统异常，请稍后重试");
        }
    }

    /**
     * 管理员登录接口
     * <p>
     * 验证管理员身份并生成JWT令牌
     *
     * @param request 登录请求参数，包含用户名和密码
     * @return 登录成功后返回用户信息和JWT令牌
     */
    @PostMapping("/login")
    public ApiResponse<AdminUserResponse> login(@Valid @RequestBody AdminUserLoginRequest request) {
        log.info("管理员登录请求: {}", request.getUsername());

        try {
            // 1. 使用 Spring Security 进行身份验证
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()));

            // 2. 将认证信息放入 Security 上下文
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 3. 获取用户信息
            AdminUser adminUser = adminUserService.getOne(
                    new LambdaQueryWrapper<AdminUser>()
                            .eq(AdminUser::getUsername, request.getUsername()));

            // 4. 更新最后登录时间
            adminUser.setLastLoginTime(LocalDateTime.now());
            adminUserService.updateById(adminUser);

            // 5. 生成 JWT 令牌
            String token = jwtUtil.generateToken(adminUser.getUsername());

            // 6. 构建响应对象
            AdminUserResponse response = buildAdminUserResponse(adminUser, token);

            log.info("管理员登录成功: {}", request.getUsername());
            return ApiResponse.success("登录成功", response);
        } catch (Exception e) {
            log.error("管理员登录异常: {}", e.getMessage(), e);
            return ApiResponse.fail(BizStatus.UNAUTHORIZED, "用户名或密码错误");
        }
    }

    /**
     * 修改管理员信息接口
     * <p>
     * 更新管理员用户的基本信息，支持更新用户名、密码、邮箱和电话
     *
     * @param request 修改请求参数，包含管理员ID和要更新的信息
     * @return 更新成功后返回用户信息
     */
    @PostMapping("/update")
    public ApiResponse<AdminUserResponse> update(@Valid @RequestBody AdminUserUpdateRequest request) {
        log.info("修改管理员信息请求: ID={}", request.getId());

        try {
            // 1. 验证管理员ID是否存在
            AdminUser adminUser = adminUserService.getById(request.getId());
            if (adminUser == null) {
                log.warn("管理员不存在: ID={}", request.getId());
                return ApiResponse.fail(BizStatus.NOT_FOUND, "管理员不存在");
            }

            // 2. 如果更新用户名，检查用户名是否已存在
            if (request.getUsername() != null && !request.getUsername().equals(adminUser.getUsername())) {
                AdminUser existingUser = adminUserService.getOne(
                        new LambdaQueryWrapper<AdminUser>()
                                .eq(AdminUser::getUsername, request.getUsername()));
                if (existingUser != null) {
                    log.warn("用户名已存在: {}", request.getUsername());
                    return ApiResponse.fail(BizStatus.CONFLICT, "用户名已存在");
                }
                adminUser.setUsername(request.getUsername());
            }

            // 3. 如果更新密码，进行密码加密处理
            if (request.getPassword() != null) {
                String encodedPassword = passwordEncoder.encode(request.getPassword());
                adminUser.setPasswordHash(encodedPassword);
            }

            // 4. 更新其他信息
            if (request.getEmail() != null) {
                adminUser.setEmail(request.getEmail());
            }
            if (request.getPhone() != null) {
                adminUser.setPhone(request.getPhone());
            }

            // 5. 保存更新到数据库
            boolean updateResult = adminUserService.updateById(adminUser);
            if (!updateResult) {
                log.error("修改管理员信息失败: ID={}", request.getId());
                return ApiResponse.fail(BizStatus.ERROR, "修改失败，请稍后重试");
            }

            // 6. 生成新的 JWT 令牌
            String token = jwtUtil.generateToken(adminUser.getUsername());

            // 7. 构建响应对象
            AdminUserResponse response = buildAdminUserResponse(adminUser, token);

            log.info("修改管理员信息成功: ID={}", request.getId());
            return ApiResponse.success("修改成功", response);
        } catch (Exception e) {
            log.error("修改管理员信息异常: {}", e.getMessage(), e);
            return ApiResponse.fail(BizStatus.ERROR, "系统异常，请稍后重试");
        }
    }

    /**
     * 删除管理员接口
     * <p>
     * 删除指定的管理员账户，添加安全校验机制
     *
     * @param request 删除请求参数，包含管理员ID
     * @return 删除成功后返回结果
     */
    @PostMapping("/delete")
    public ApiResponse<?> delete(@Valid @RequestBody AdminUserDeleteRequest request) {
        log.info("删除管理员请求: ID={}", request.getId());

        try {
            // 1. 验证管理员ID是否存在
            AdminUser adminUser = adminUserService.getById(request.getId());
            if (adminUser == null) {
                log.warn("管理员不存在: ID={}", request.getId());
                return ApiResponse.fail(BizStatus.NOT_FOUND, "管理员不存在");
            }

            // 2. 执行删除操作
            boolean deleteResult = adminUserService.removeById(request.getId());
            if (!deleteResult) {
                log.error("删除管理员失败: ID={}", request.getId());
                return ApiResponse.fail(BizStatus.ERROR, "删除失败，请稍后重试");
            }

            log.info("删除管理员成功: ID={}", request.getId());
            return ApiResponse.success("删除成功");
        } catch (Exception e) {
            log.error("删除管理员异常: {}", e.getMessage(), e);
            return ApiResponse.fail(BizStatus.ERROR, "系统异常，请稍后重试");
        }
    }

    /**
     * 构建管理员用户响应对象
     * <p>
     * 将管理员用户信息和令牌封装为响应对象
     *
     * @param adminUser 管理员用户实体
     * @param token     JWT令牌
     * @return 管理员用户响应对象
     */
    private AdminUserResponse buildAdminUserResponse(AdminUser adminUser, String token) {
        AdminUserResponse response = new AdminUserResponse();
        response.setId(adminUser.getId());
        response.setUsername(adminUser.getUsername());
        response.setToken(token);
        response.setRole(adminUser.getRole());
        response.setEmail(adminUser.getEmail());
        response.setPhone(adminUser.getPhone());
        return response;
    }
}
