package com.yan.xiaoyanqtserve.common.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 管理员登录请求DTO
 * 包含登录所需的用户名和密码字段
 */
@Data
public class AdminUserLoginRequest {

    /**
     * 用户名
     * 不能为空
     */
    @NotBlank(message = "用户名不能为空")
    private String username;

    /**
     * 密码
     * 不能为空
     */
    @NotBlank(message = "密码不能为空")
    private String password;
}
