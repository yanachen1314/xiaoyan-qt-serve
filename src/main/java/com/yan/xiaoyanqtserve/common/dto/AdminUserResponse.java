package com.yan.xiaoyanqtserve.common.dto;

import lombok.Data;

/**
 * 管理员用户响应DTO
 * 包含用户信息和登录成功后的令牌
 */
@Data
public class AdminUserResponse {

    /**
     * 用户ID
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * JWT令牌
     */
    private String token;

    /**
     * 角色
     */
    private String role;

    /**
     * 邮箱地址
     */
    private String email;

    /**
     * 联系电话
     */
    private String phone;
}
