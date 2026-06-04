package com.yan.xiaoyanqtserve.config.custome;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 豆包工具 Flask 解析服务代理配置。
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "xiaoyan.tools.doubao")
public class DoubaoToolPropertiesConfig {
    /**
     * Flask 豆包接口基础地址，末尾可带或不带 /。
     */
    private String baseUrl = "http://127.0.0.1:5000/api/doubao";

    /**
     * 连接超时时间，单位：毫秒。
     */
    private int connectTimeoutMillis = 5000;

    /**
     * 读取超时时间，单位：毫秒。
     */
    private int readTimeoutMillis = 30000;
}
