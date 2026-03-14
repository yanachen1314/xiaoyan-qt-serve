package com.yan.xiaoyanqtserve.service;

import com.yan.xiaoyanqtserve.entity.AdminUser;
import com.yan.xiaoyanqtserve.common.dto.adminUser.AdminUserUpdateRequest;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 管理员用户表 服务类
 * </p>
 *
 * @author 烟尘
 * @since 2026-02-03
 */
public interface AdminUserService extends IService<AdminUser> {

    /**
     * 根据用户名查找用户
     * @param username 用户名
     * @return 用户实体
     */
    AdminUser findByUsername(String username);

    /**
     * 更新管理员信息
     * @param adminUser 管理员实体
     * @return 操作结果
     */
    boolean updateAdminUser(AdminUser adminUser);

    /**
     * 根据更新请求更新管理员信息
     * @param updateRequest 更新请求参数
     * @return 操作结果
     */
    boolean updateAdminUserByRequest(AdminUserUpdateRequest updateRequest);

    /**
     * 删除管理员
     * @param id 管理员 ID
     * @return 操作结果
     */
    boolean deleteAdminUser(Long id);

    /**
     * 根据手机号或邮箱重置密码
     * @param contact 手机号或邮箱
     * @param newPassword 新密码
     * @return 操作结果
     */
    boolean resetPasswordByContact(String contact, String newPassword);
}
