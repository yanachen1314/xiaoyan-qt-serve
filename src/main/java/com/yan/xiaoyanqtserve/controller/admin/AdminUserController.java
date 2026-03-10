package com.yan.xiaoyanqtserve.controller.admin;

import com.yan.xiaoyanqtserve.common.dto.adminUser.AdminUserDeleteRequest;
import com.yan.xiaoyanqtserve.common.dto.adminUser.AdminUserUpdateRequest;
import com.yan.xiaoyanqtserve.entity.AdminUser;
import com.yan.xiaoyanqtserve.service.AdminUserService;
import com.yan.xiaoyanqtserve.common.utils.ApiResponse;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
