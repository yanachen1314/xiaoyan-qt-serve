package com.yan.xiaoyanqtserve.common.dto.adminUser;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 密码重置请求 DTO
 * 包含手机号或邮箱以及新密码
 */
@Data
public class PasswordResetRequest {

    /**
     * 手机号或邮箱
     * 不能为空
     */
    @NotBlank(message = "手机号或邮箱不能为空")
    private String contact;

    /**
     * 新密码
     * 不能为空，至少 8 位
     */
    @NotBlank(message = "新密码不能为空")
    private String newPassword;
}
