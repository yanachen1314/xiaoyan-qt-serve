package com.yan.xiaoyanqtserve.config.custome;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 邮箱配置属性
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "xiaoyan.mail")
public class MailPropertiesConfig {
    // 验证链接配置
    private VerifyLinkConfig verifyLink;
    // 安全限制配置
    private SafetyLimitsConfig safetyLimits;

    public MailPropertiesConfig() {
        this.verifyLink = new VerifyLinkConfig();
        this.safetyLimits = new SafetyLimitsConfig();
    }

    /**
     * 邮箱验证链接配置
     */
    @Data
    public static class VerifyLinkConfig {
        // 邮箱验证验证链接长度
        private Integer tokenLength;
        // 邮箱验证链接有效期（分钟）
        private Integer expireTime;
        // 链接校验基础路径
        private String basePath;
    }

    /**
     * 邮件发送安全限制配置
     */
    @Data
    public static class SafetyLimitsConfig {
        // 日发送上限总额（所有邮箱合计）
        private Integer dailyTotalMax = 100;


        // 单邮箱每天上限
        private Integer mailDailyMax = 50;
        //单邮箱每分钟发送速率
        private Integer mailMinuteRate = 5;
        //单邮箱退信率阈值，超过此值立即禁用（如 0.1 表示 10%）
        private Double mailBounceRate = 0.1;
    }

}