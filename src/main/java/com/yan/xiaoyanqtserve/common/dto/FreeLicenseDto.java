package com.yan.xiaoyanqtserve.common.dto;

import jakarta.validation.constraints.Email;
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
    private String email;
}
