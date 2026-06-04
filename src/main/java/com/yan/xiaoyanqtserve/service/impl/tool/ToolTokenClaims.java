package com.yan.xiaoyanqtserve.service.impl.tool;

/**
 * 工具 JWT 解析后的业务声明。
 *
 * @param toolCode 工具编码
 * @param deviceIdHash 浏览器指纹哈希
 * @param openIdHash 微信 openId 哈希
 * @param jti JWT 唯一编号
 */
public record ToolTokenClaims(
        String toolCode,
        String deviceIdHash,
        String openIdHash,
        String jti
) {
}
