package com.yan.xiaoyanqtserve.common.dto.adminUser;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * 邮件账户新增/更新DTO
 *
 * @author 烟尘
 * @since 2026-05-14
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class MailAccountDto {
    /**
     * 主键ID（更新时必填）
     */
    private Long id;

    /**
     * 邮箱地址
     */
    @NotBlank(message = "邮箱地址不能为空")
    @Email(message = "邮箱地址格式不正确")
    private String emailAddress;

    /**
     * SMTP服务器地址
     */
    @NotBlank(message = "SMTP服务器地址不能为空")
    private String host;

    /**
     * SMTP端口
     */
    @NotNull(message = "SMTP端口不能为空")
    @Min(value = 1, message = "端口号必须大于等于 1")
    @Max(value = 65535, message = "端口号不能超过 65535")
    private Integer port;

    /**
     * 邮箱密码/授权码
     */
    @NotBlank(message = "邮箱密码/授权码不能为空")
    private String password;

    /**
     * 协议类型
     */
    @NotBlank(message = "协议类型不能为空")
    private String protocol;

    /**
     * 是否启用SSL: 0-否, 1-是
     */
    @NotNull(message = "SSL配置不能为空")
    private Byte sslEnable;

    /**
     * 状态: 0-禁用, 1-启用
     */
    @NotNull(message = "状态不能为空")
    private Byte status;

    /**
     * 每日发送上限
     */
    @Min(value = 0, message = "每日发送上限不能小于 0")
    private Integer dailyLimit;

    /**
     * 优先级，数值越小优先级越高
     */
    @Min(value = 1, message = "优先级必须大于等于 1")
    private Integer priority;

    /**
     * 备注
     */
    private String remark;

}
