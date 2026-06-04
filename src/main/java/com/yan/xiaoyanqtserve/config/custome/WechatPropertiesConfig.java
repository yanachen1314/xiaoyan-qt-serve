package com.yan.xiaoyanqtserve.config.custome;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 微信公众号回调配置属性。
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "xiaoyan.wechat")
public class WechatPropertiesConfig {
    /**
     * 微信公众号服务器配置中的 Token。
     */
    private String token = "asdhjksfdgdfg";
}
