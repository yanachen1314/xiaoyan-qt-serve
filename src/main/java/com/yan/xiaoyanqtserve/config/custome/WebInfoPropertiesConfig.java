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
     * 网站主站点
     */
    private String mainSite;
    /**
     * 网站管理站点
     */
    private String adminSite;
    /**
     * 小烟改写工具 WEB端部署域名
     */
    private String writerSite;
    /**
     * 网站接口站点
     */
    private String apiSite;

    /**
     * 商品购买地址
     */
    private String ldxpUrl;

}