package com.yan.xiaoyanqtserve.common.dto.tool;

import java.io.Serializable;

/**
 * Redis 中保存的微信工具验证码信息。
 *
 * @param toolCode 工具编码
 * @param openIdHash 微信 openId 哈希
 * @param createdAt 创建时间戳
 */
public record ToolVerificationCode(
        String toolCode,
        String openIdHash,
        long createdAt
) implements Serializable {
}
