package com.yan.xiaoyanqtserve.common.enums;

import lombok.Getter;

/**
 * 请求类型枚举
 * @author yanchen
 * @since 2026/3/16 17:41
 */
@Getter
public enum RequestType {
    PUBLIC_REQUEST("PUBLIC", "/api/public", "公开请求"),
    ADMIN_REQUEST("ADMIN", "/api/admin", "管理员请求"),
    APIKEY_REQUEST("APIKEY", "/api/key", "卡密请求"),
    UNKNOWN("UNKNOWN", "", "未知请求");

    /**
     * 类型编码
     */
    private final String code;
    /**
     * url前缀
     */
    private final String urlPrefix;
    /**
     * 描述
     */
    private final String description;

    RequestType(String code, String urlPrefix, String description) {
        this.code = code;
        this.urlPrefix = urlPrefix;
        this.description = description;
    }

    /**
     * 根据url获取请求类型
     * @param url url
     * @return 请求类型
     */
    public static RequestType getByUrl(String url) {
        for (RequestType type : RequestType.values()) {
            if (url.startsWith(type.urlPrefix)) {
                return type;
            }
        }
        return RequestType.UNKNOWN;
    }

    /**
     * 判断是否为未知请求类型
     * @param code 请求类型编码
     * @return 是否为未知请求类型
     */
    public static Boolean isUnknownByCode(String code) {
        for (RequestType type : RequestType.values()) {
            if (type.code.equals(code) && !type.equals(RequestType.UNKNOWN)) {
                return false;
            }
        }
        return RequestType.UNKNOWN.code.equals(code);
    }

    /**
     * 判断是否为未知请求类型
     * @param urlPrefix url前缀
     * @return 是否为未知请求类型
     */
    public static Boolean isUnknownByUrlPrefix(String urlPrefix) {
        for (RequestType type : RequestType.values()) {
            if (type.urlPrefix.equals(urlPrefix) && !type.equals(RequestType.UNKNOWN)) {
                return false;
            }
        }
        return RequestType.UNKNOWN.urlPrefix.equals(urlPrefix);
    }
}
