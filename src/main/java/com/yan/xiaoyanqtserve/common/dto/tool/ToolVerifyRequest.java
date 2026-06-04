package com.yan.xiaoyanqtserve.common.dto.tool;

import jakarta.validation.constraints.NotBlank;

/**
 * 工具验证码校验请求。
 *
 * @param toolCode 工具编码
 * @param code 微信公众号返回的验证码
 */
public record ToolVerifyRequest(
        @NotBlank(message = "工具编码不能为空")
        String toolCode,
        @NotBlank(message = "验证码不能为空")
        String code
) {
}
