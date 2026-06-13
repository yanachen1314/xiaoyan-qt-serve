package com.yan.xiaoyanqtserve.common.dto.tool;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

/**
 * Redis 中保存的工具 JWT 授权状态。
 *
 * @param toolCode 工具编码
 * @param deviceIdHash 浏览器指纹哈希
 * @param openIdHash 微信 openId 哈希
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ToolTokenState(
        String toolCode,
        String deviceIdHash,
        String openIdHash
) implements Serializable {
}
