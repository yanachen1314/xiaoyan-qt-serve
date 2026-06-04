package com.yan.xiaoyanqtserve.config.custome;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 站点工具使用校验配置。
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "xiaoyan.tool-auth")
public class ToolAuthPropertiesConfig {
    /**
     * 微信验证码有效期，单位：分钟。
     */
    private int codeExpireMinutes = 5;

    /**
     * 工具 JWT 有效期，单位：分钟。
     */
    private int tokenExpireMinutes = 10;

    /**
     * 单个工具 JWT 可使用次数。
     */
    private int tokenUses = 1;

    /**
     * 同一设备在锁定窗口内允许失败的最大次数。
     */
    private int verifyFailLimit = 5;

    /**
     * 验证失败锁定窗口，单位：分钟。
     */
    private int verifyLockMinutes = 10;
}
