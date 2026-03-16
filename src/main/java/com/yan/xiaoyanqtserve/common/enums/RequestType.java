package com.yan.xiaoyanqtserve.common.enums;

import lombok.Getter;

/**
 * 请求类型枚举
 * @author yanchen
 * @since 2026/3/16 17:41
 */
@Getter
public enum RequestType {
    PUBLIC_REQUEST("公开请求", "/api/public"),
    ADMIN_REQUEST("管理员请求", "/api/admin"),
    APIKEY_REQUEST("卡密请求", "/api/key");

    /**
     * url前缀
     */
    private final String urlPrefix;
    /**
     * 描述
     */
    private final String description;

    RequestType(String description, String urlPrefix) {
        this.description = description;
        this.urlPrefix = urlPrefix;
    }
}
