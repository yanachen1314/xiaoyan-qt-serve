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

    /**
     * 微信公众号 AppID，用于校验加密消息明文末尾的 AppID。
     */
    private String appId;

    /**
     * 微信公众号 AppSecret，用于获取访问令牌。
     */
    private String appSecret;

    /**
     * 微信公众号消息加解密密钥 EncodingAESKey，安全模式下必填。
     */
    private String encodingAesKey;
}
