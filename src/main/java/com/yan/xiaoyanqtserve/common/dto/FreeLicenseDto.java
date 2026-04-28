package com.yan.xiaoyanqtserve.common.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 免费密钥申请Dto
 *
 * @author yanchen
 * @since 2026/4/22 22:29
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FreeLicenseDto {
    /**
     * 验证码参数(阿里云验证码服务端返回的验证码参数)
     */
    @NotBlank(message = "验证码参数不能为空")
    private String captchaVerifyParam;

    /**
     * 申请者邮箱
     */
    @Email(message = "邮箱格式不正确")
    @NotBlank(message = "邮箱不能为空")
    private String email;

}

