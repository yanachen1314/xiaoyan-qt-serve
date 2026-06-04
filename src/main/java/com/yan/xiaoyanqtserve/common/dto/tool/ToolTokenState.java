package com.yan.xiaoyanqtserve.common.dto.tool;

import java.io.Serializable;

/**
 * Redis 中保存的工具 JWT 一次性消费状态。
 *
 * @param toolCode 工具编码
 * @param deviceIdHash 浏览器指纹哈希
 * @param openIdHash 微信 openId 哈希
 * @param remainingUses 剩余使用次数
 */
public record ToolTokenState(
        String toolCode,
        String deviceIdHash,
        String openIdHash,
        int remainingUses
) implements Serializable {
}
