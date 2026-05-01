package com.yan.xiaoyanqtserve.config.custome;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 阿里云验证码配置属性
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "aliyun.captcha")
public class CaptchaProperties {
    /**
     * AccessKey ID（必须）
     */
    private String accessKeyId;

    /**
     * AccessKey Secret（必须）
     */
    private String accessKeySecret;

    /**
     * 服务端点（默认：captcha.cn-shanghai.aliyuncs.com）
     */
    private String endpoint = "captcha.cn-shanghai.aliyuncs.com";

    /**
     * 连接超时（毫秒，默认：5000）
     */
    private Integer connectTimeout = 5000;

    /**
     * 读取超时（毫秒，默认：5000）
     */
    private Integer readTimeout = 5000;

    /**
     * Ekey（必须）
     */
    private String ekey;
}