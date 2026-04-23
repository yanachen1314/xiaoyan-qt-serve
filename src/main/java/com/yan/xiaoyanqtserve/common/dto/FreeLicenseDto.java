package com.yan.xiaoyanqtserve.common.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 免费密钥申请Dto
 * @author yanchen
 * @since 2026/4/22 22:29
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FreeLicenseDto {
    /**
     * 申请者邮箱
     */
    @Email(message = "邮箱格式不正确")
    @NotBlank(message = "邮箱不能为空")
    private String email;

    /**
     * 设备ID/设备指纹（由请求解析器自动填充）
     */
    private String deviceId;

    /**
     * 客户端IP地址（由请求解析器自动填充）
     */
    private String clientIp;

    /**
     * 用户代理信息（由请求解析器自动填充）
     */
    private String userAgent;
}

