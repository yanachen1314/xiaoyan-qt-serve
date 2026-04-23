package com.yan.xiaoyanqtserve.config;

import com.yan.xiaoyanqtserve.interceptor.RequestLoggingInterceptor;
import com.yan.xiaoyanqtserve.resolver.FreeLicenseDtoArgumentResolver;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor;

import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Resource
    private RequestLoggingInterceptor requestLoggingInterceptor;

    @Autowired
    private List<HttpMessageConverter<?>> messageConverters;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(requestLoggingInterceptor)
                .addPathPatterns("/**");
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new FreeLicenseDtoArgumentResolver(
                new RequestResponseBodyMethodProcessor(messageConverters)
        ));
    }
}