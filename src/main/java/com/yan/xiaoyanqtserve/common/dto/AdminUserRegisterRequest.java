package com.yan.xiaoyanqtserve.common.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 管理员注册请求DTO
 * 包含注册所需的用户名、密码、邮箱和电话字段
 */
@Data
public class AdminUserRegisterRequest {

    /**
     * 用户名
     * 不能为空，长度在3-50之间
     */
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度必须在3-50之间")
    private String username;

    /**
     * 密码
     * 不能为空，长度至少8位，包含字母和数字
     */
    @NotBlank(message = "密码不能为空")
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
