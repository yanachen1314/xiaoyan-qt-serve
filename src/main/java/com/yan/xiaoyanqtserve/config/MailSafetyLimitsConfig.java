package com.yan.xiaoyanqtserve.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 邮件发送安全限制配置
 * <p>从 spring.mail.safety_limits 读取安全限制参数</p>
 * @author yanchen
 * @since 2026/4/23
 */
@Data
@Component
@ConfigurationProperties(prefix = "spring.mail.safety-limits")
public class MailSafetyLimitsConfig {

    /**
     * 日发送上限总额（所有邮箱合计）
     */
    private Integer dailyTotalMax = 100;

    /**
     * 单邮箱每天上限
     */
    private Integer mailDailyMax = 50;

    /**
     * 单邮箱每分钟发送速率
     */
    private Integer mailMinuteRate = 5;

    /**
     * 单邮箱退信率阈值，超过此值立即禁用（如 0.1 表示 10%）
     */
    private Double mailBounceRate = 0.1;
}
