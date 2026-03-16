package com.yan.xiaoyanqtserve.config;

import com.yan.xiaoyanqtserve.interceptor.RequestLoggingInterceptor;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Resource
    private RequestLoggingInterceptor requestLoggingInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 添加你的日志拦截器，并指定它应用于所有路径
        registry.addInterceptor(requestLoggingInterceptor)
                .addPathPatterns("/**") // 拦截所有请求
        ;
    }
}