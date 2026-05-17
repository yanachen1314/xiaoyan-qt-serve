package com.yan.xiaoyanqtserve.config.custome;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * CORS 跨域配置属性
 * 用于从 YAML 配置文件中读取 CORS 相关配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "xiaoyan.cors")
public class CorsConfigProperties {

    /**
     * 允许访问的源地址列表
     * 示例: ["http://localhost:5173", "https://example.com"]
     */
    private List<String> allowedOriginPatterns = List.of("*");

    /**
     * 允许的 HTTP 请求方法列表
     * 示例: ["GET", "POST", "PUT", "DELETE", "OPTIONS"]
     */
    private List<String> allowedMethods = List.of("GET", "POST", "PUT", "DELETE", "OPTIONS");

    /**
     * 允许的请求头列表
     * 示例: ["Authorization", "Content-Type", "X-Device"]
     */
    private List<String> allowedHeaders = List.of("Authorization", "Content-Type", "X-Device", "X-Client-Version", "X-API-Key");

    /**
     * 允许暴露的响应头列表
     * 示例: ["Authorization"]
     */
    private List<String> exposedHeaders = List.of("Authorization");

    /**
     * 预检请求缓存时间（秒）
     */
    private Long maxAge = 3600L;

    /**
     * 是否允许携带 Cookie
     */
    private Boolean allowCredentials = true;
}
