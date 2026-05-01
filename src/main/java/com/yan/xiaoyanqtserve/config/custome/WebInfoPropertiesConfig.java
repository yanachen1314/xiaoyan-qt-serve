package com.yan.xiaoyanqtserve.config.custome;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
/**
 * 阿里云验证码配置属性
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "xiaoyan.webinfo")
public class WebInfoPropertiesConfig {
    /**
     * 网站名称
     */
    private String name;

    /**
     * 网站域名
     */
    private String domain;

    /**
     * 商品购买地址
     */
    private String ldxpUrl = "captcha.cn-shanghai.aliyuncs.com";

}