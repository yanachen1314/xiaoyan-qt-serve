package com.yan.xiaoyanqtserve.common.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 管理员信息更新请求DTO
 * 包含更新管理员所需的字段
 */
@Data
public class AdminUserUpdateRequest {

    /**
     * 管理员ID
     * 不能为空
     */
    private Long id;

    /**
     * 用户名
     * 可选，长度在3-50之间
     */
    @Size(min = 3, max = 50, message = "用户名长度必须在3-50之间")
    private String username;

    /**
     * 密码
     * 可选，长度至少8位，包含字母和数字
     */
    @Size(min = 8, message = "密码长度至少8位")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$", message = "密码必须包含字母和数字")
    private String password;

    /**
     * 邮箱地址
     * 可选，格式必须正确
     */
    @Email(message = "邮箱格式不正确")
    private String email;

    /**
     * 联系电话
     * 可选，格式必须正确
     */
    @Pattern(regexp = "^$|^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;
}
