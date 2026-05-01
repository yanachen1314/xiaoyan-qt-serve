package com.yan.xiaoyanqtserve;

import com.yan.xiaoyanqtserve.config.XiaoyanSecurityConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableConfigurationProperties(XiaoyanSecurityConfig.class)
@EnableAsync
@EnableRetry
@EnableScheduling
@SpringBootApplication
public class XiaoyanQtServeApplication {

    public static void main(String[] args) {
        SpringApplication.run(XiaoyanQtServeApplication.class, args);
    }

}
