package com.yan.xiaoyanqtserve.common.enums;

/**
 * 客户端类型
 * @author yanchen
 * @since 2026/2/3 17:38
 */
public enum ClientType {
    /**
     * 未知客户端
     */
    UNKNOWN,
    /**
     * WEB管理端
     */
    ADMIN,

    /**
     * 用户 WEB 端
     */
    WEB,
    /**
     * windows客户端
     */
    WIN,
    /**
     * 手机端
     */
    APP,
    /**
     * 小程序
     */
    MINI_PROGRAM;


    /**
     * 根据名称获取 ClientType 枚举
     * @param name 客户端类型名称
     * @return ClientType 枚举，未找到返回 {@link ClientType#UNKNOWN}
     */
    public static ClientType of(String name) {
        for (ClientType status : ClientType.values()) {
            if (status.name().equalsIgnoreCase(name)) {
                return status;
            }
        }
        return UNKNOWN;
    }
}
