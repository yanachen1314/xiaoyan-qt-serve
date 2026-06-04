package com.yan.xiaoyanqtserve.common.dto.tool;

/**
 * 工具验证码换取 JWT 后的响应。
 *
 * @param toolToken 工具 JWT
 * @param expiresIn 剩余有效期，单位：秒
 * @param remainingUses 剩余使用次数
 */
public record ToolVerifyResponse(String toolToken, long expiresIn, int remainingUses) {
}
