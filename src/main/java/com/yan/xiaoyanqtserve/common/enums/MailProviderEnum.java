package com.yan.xiaoyanqtserve.common.enums;

import lombok.Getter;

/**
 * 邮箱提供商枚举
 * @author yanchen
 * @since 2026/4/22
 */
@Getter
public enum MailProviderEnum {
    
    QQ("QQ", "smtp.qq.com", 465, true),
    
    NETEASE_163("163", "smtp.163.com", 465, true),
    
    NETEASE_126("126", "smtp.126.com", 465, true),
    
    CUSTOM("custom", null, null, null);
    
    private final String providerName;
    private final String defaultHost;
    private final Integer defaultPort;
    private final Boolean defaultSslEnable;
    
    MailProviderEnum(String providerName, String defaultHost, Integer defaultPort, Boolean defaultSslEnable) {
        this.providerName = providerName;
        this.defaultHost = defaultHost;
        this.defaultPort = defaultPort;
        this.defaultSslEnable = defaultSslEnable;
    }
    
    public static MailProviderEnum fromHost(String host) {
        if (host == null) {
            return CUSTOM;
        }
        for (MailProviderEnum provider : values()) {
            if (provider.defaultHost != null && provider.defaultHost.equals(host)) {
                return provider;
            }
        }
        return CUSTOM;
    }
}