package com.yan.xiaoyanqtserve.common.enums;

/**
 * 客户端类型
 * @author yanchen
 * @since 2026/2/3 17:38
 */
public enum ClientType {
    /**
     * WEB管理端
     */
    ADMIN,
    /**
     * 后台服务
     */
    SERVER,

    /**
     * 用户 WEB 端
     */
    WEB,
    /**
     * windows客户端
     */
    WINDOWS,
    /**
     * 手机端
     */
    APP,
    /**
     * 小程序
     */
    MINI_PROGRAM;

    /**
     * 判断是否为该类型，忽略大小写
     * @param type 客户端类型
     * @return 是否为该类型
     */
    public boolean equals(String type) {
        if (type == null || type.isEmpty()) {
            return false;
        }
        return this.name().equalsIgnoreCase(type);
    }
}
