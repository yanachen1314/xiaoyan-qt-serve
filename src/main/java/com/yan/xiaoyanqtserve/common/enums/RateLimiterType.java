package com.yan.xiaoyanqtserve.common.enums;

/**
 * 限流类型枚举
 * @author yanchen
 * @since 2026/3/10 21:15
 */
public enum RateLimiterType {
    /**
     * IP限流
     */
    IP,
    /**
     * API_KEY限流
     */
    API_KEY,
    /**
     * 设备ID/设备指纹限流
     */
    DEVICE_ID
}
