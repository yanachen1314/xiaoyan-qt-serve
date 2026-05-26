package com.yan.xiaoyanqtserve.controller.admin;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yan.xiaoyanqtserve.common.dto.adminUser.AdminUserDeleteRequest;
import com.yan.xiaoyanqtserve.common.dto.adminUser.AdminUserPageRequest;
import com.yan.xiaoyanqtserve.common.dto.adminUser.AdminUserRegisterRequest;
import com.yan.xiaoyanqtserve.common.dto.adminUser.AdminUserUpdateRequest;
import com.yan.xiaoyanqtserve.entity.AdminUser;
import com.yan.xiaoyanqtserve.service.AdminUserService;
import com.yan.xiaoyanqtserve.common.utils.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * 【管理后台】用户控制器
 * 处理管理员登录、注册、修改、删除等认证相关请求
 *
 * @author 烟尘
 * @since 2026-02-04
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/user")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    private final PasswordEncoder passwordEncoder;

    /**
     * 获取管理员分页列表
     *
     * @param pageRequest 分页查询参数
     * @return 分页结果
     */
    @PostMapping("/list")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Page<AdminUser>> list(@RequestBody AdminUserPageRequest pageRequest) {
        Page<AdminUser> adminUserPage = new Page<>(pageRequest.getCurrentPage(), pageRequest.getPageSize());
        
        QueryWrapper<AdminUser> queryWrapper = new QueryWrapper<>();
        // 多条件模糊查询
        if (StrUtil.isNotBlank(pageRequest.getUsername())) {
            queryWrapper.like("username", pageRequest.getUsername());
        }
        if (StrUtil.isNotBlank(pageRequest.getEmail())) {
            queryWrapper.like("email", pageRequest.getEmail());
        }
        if (StrUtil.isNotBlank(pageRequest.getPhone())) {
            queryWrapper.like("phone", pageRequest.getPhone());
        }
        queryWrapper.orderByDesc("id");
        
        Page<AdminUser> result = adminUserService.page(adminUserPage, queryWrapper);
        return ApiResponse.success(result);
    }

    /**
     * 添加管理员
     *
     * @param registerRequest 注册请求参数
     * @return 添加结果
     */
    @PostMapping("/register")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<String> register(@Valid @RequestBody AdminUserRegisterRequest registerRequest) {
        // 检查用户名是否已存在
        AdminUser existingUser = adminUserService.findByUsername(registerRequest.getUsername());
        if (existingUser != null) {
            return ApiResponse.fail("用户名已存在");
        }

        // 创建新管理员
        AdminUser adminUser = new AdminUser();
        adminUser.setUsername(registerRequest.getUsername());
        adminUser.setPasswordHash(passwordEncoder.encode(registerRequest.getPassword()));
        adminUser.setRole("ADMIN");
        adminUser.setEmail(registerRequest.getEmail());
        adminUser.setPhone(registerRequest.getPhone());
        adminUser.setCreatedTime(LocalDateTime.now());
        adminUser.setUpdatedTime(LocalDateTime.now());

        // 保存管理员
        boolean result = adminUserService.save(adminUser);
        if (result) {
            return ApiResponse.success("添加成功");
        } else {
            return ApiResponse.fail("添加失败");
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
