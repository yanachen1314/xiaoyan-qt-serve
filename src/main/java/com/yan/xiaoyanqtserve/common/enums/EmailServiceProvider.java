package com.yan.xiaoyanqtserve.common.enums;

import lombok.Getter;

import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

/**
 * 邮箱服务商枚举
 * 记录各大邮箱服务商信息，包括常用域名和授权密码有效期等特性
 */
@Getter
public enum EmailServiceProvider {

    /**
     * 网易邮箱系列 (163, 126, yeah等)
     * 授权密码有效期：180天
     */
    NETEASE("网易邮箱", 
            Arrays.asList("163.com", "126.com", "yeah.net", "vip.163.com", "163.net", "vip.126.com", "126.net", "vip.163.net", "vip.126.net"),
            180, ChronoUnit.DAYS),
    
    /**
     * 腾讯邮箱系列 (QQ邮箱, Foxmail)
     * 授权密码有效期：30天
     */
    TENCENT("腾讯邮箱", 
            Arrays.asList("qq.com", "foxmail.com", "vip.qq.com", "exmail.qq.com"),
            Integer.MAX_VALUE, ChronoUnit.DAYS),
    
    /**
     * 新浪邮箱系列
     * 授权密码有效期：180天(根据最新政策可能有变化)
     */
    SINA("新浪邮箱", 
         Arrays.asList("sina.com", "sina.cn", "vip.sina.com", "vip.sina.com.cn"),
         180, ChronoUnit.DAYS),
    
    /**
     * 搜狐邮箱
     * 授权密码有效期：180天(根据最新政策可能有变化)
     */
    SOHU("搜狐邮箱", 
         Arrays.asList("sohu.com", "139.com", "vip.sohu.com"),
         180, ChronoUnit.DAYS),
    
    /**
     * 微软邮箱 (Hotmail/Outlook)
     * 授权密码有效期：长期有效(但建议定期更换)
     */
    MICROSOFT("微软邮箱", 
              Arrays.asList("hotmail.com", "outlook.com", "live.com", "msn.com"),
              Integer.MAX_VALUE, ChronoUnit.DAYS),
    
    /**
     * Google邮箱
     * 授权密码有效期：长期有效(但建议定期更换)
     */
    GOOGLE("Gmail", 
           Arrays.asList("gmail.com", "googlemail.com"),
           Integer.MAX_VALUE, ChronoUnit.DAYS),
    
    /**
     * 雅虎邮箱
     * 授权密码有效期：长期有效(但建议定期更换)
     */
    YAHOO("雅虎邮箱", 
          Arrays.asList("yahoo.com", "yahoo.cn", "yahoo.com.cn", "yahoo.com.hk", "ymail.com", "rocketmail.com"),
          Integer.MAX_VALUE, ChronoUnit.DAYS),
    
    /**
     * 阿里云邮箱
     * 授权密码有效期：180天(根据阿里云最新政策)
     */
    ALIBABA("阿里云邮箱", 
            Arrays.asList("aliyun.com", "wanwang.com", "aliyun-inc.com"),
            180, ChronoUnit.DAYS),
    
    /**
     * 中国移动企业邮箱
     */
    CHINA_MOBILE("中国移动企业邮箱", 
                 Arrays.asList("139.com", "10086.cn", "chinamobile.com"),
                 180, ChronoUnit.DAYS),
    
    /**
     * 其他未知邮箱服务商
     */
    OTHER("其他邮箱",
            List.of(),
          180, ChronoUnit.DAYS);

    private final String displayName;
    private final List<String> domains;
    private final int passwordValidityPeriod;
    private final ChronoUnit validityUnit;

    EmailServiceProvider(String displayName, List<String> domains, int passwordValidityPeriod, ChronoUnit validityUnit) {
        this.displayName = displayName;
        this.domains = domains;
        this.passwordValidityPeriod = passwordValidityPeriod;
        this.validityUnit = validityUnit;
    }

    /**
     * 根据邮箱地址获取对应的服务商
     * @param emailAddress 邮箱地址
     * @return 对应的邮箱服务商枚举
     */
    public static EmailServiceProvider fromEmailAddress(String emailAddress) {
        if (emailAddress == null || !emailAddress.contains("@")) {
            return OTHER;
        }
        
        String domain = emailAddress.substring(emailAddress.lastIndexOf('@') + 1).toLowerCase();
        
        for (EmailServiceProvider provider : values()) {
            if (provider != OTHER) {
                for (String d : provider.domains) {
                    if (domain.equals(d) || (d.startsWith("*") && domain.endsWith(d.substring(1)))) {
                        return provider;
                    }
                }
            }
        }
        
        return OTHER;
    }

    /**
     * 检查域名是否属于该服务商
     * @param domain 域名
     * @return 是否匹配
     */
    public boolean matchesDomain(String domain) {
        if (domain == null) return false;
        domain = domain.toLowerCase();

        String finalDomain = domain;
        return domains.stream().anyMatch(d -> finalDomain.equals(d) ||
                (d.startsWith("*") && finalDomain.endsWith(d.substring(1)))
        );
    }

    /**
     * 获取该服务商的过期提醒阈值（默认为有效期的1/6，至少7天）
     */
    public int getExpiryWarningThreshold() {
        // 对于长期有效的，不设置提醒
        if (passwordValidityPeriod == Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        // 至少7天提醒
        return Math.max(7, passwordValidityPeriod / 6);
    }


    /**
     * 获取所有域名的字符串表示，用于显示
     */
    public String getAllDomainsString() {
        return String.join(", ", domains);
    }
}