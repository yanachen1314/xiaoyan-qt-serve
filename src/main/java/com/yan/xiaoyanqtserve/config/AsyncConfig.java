package com.yan.xiaoyanqtserve.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * 异步处理配置类，用于配置异步任务执行器
 */
@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

    @Bean(name = "loggingExecutor")
    public Executor loggingExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2); // 核心线程数
        executor.setMaxPoolSize(5);  // 最大线程数
        executor.setQueueCapacity(100); // 队列大小
        executor.setThreadNamePrefix("AsyncLog-"); // 线程名前缀
        executor.initialize();
        return executor;
    }

}