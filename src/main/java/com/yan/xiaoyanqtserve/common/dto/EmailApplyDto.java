package com.yan.xiaoyanqtserve.common.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 邮箱验证申请DTO
 * @author yanchen
 * @since 2026/4/30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailApplyDto {
    
    /**
     * 申请邮箱地址
     */
    @NotBlank(message = "邮箱地址不能为空")
    @Email(message = "邮箱地址格式不正确")
    private String email;
}
