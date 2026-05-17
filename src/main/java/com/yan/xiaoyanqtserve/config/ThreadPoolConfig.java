package com.yan.xiaoyanqtserve.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@EnableAsync
@Configuration
public class ThreadPoolConfig {

    // 服务器核心线程数
    private static final int CORE_POOL_SIZE = Runtime.getRuntime().availableProcessors();

    /**
     * 自定义AI模型服务处理任务的线程池
     * @return ThreadPoolTaskExecutor
     */
    @Bean(name = "aigcModelThreadPool")
    public ThreadPoolTaskExecutor aigcModelThreadPool() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(CORE_POOL_SIZE);  // 根据模型服务能力调整
        executor.setMaxPoolSize(CORE_POOL_SIZE * 2);   // 最大线程数，根据需要调整
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("AigcModel-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }


    /**
     * 日志保存异步任务线程池
     */
    @Bean(name = "loggingExecutor")
    public Executor loggingExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(CORE_POOL_SIZE); // 核心线程数
        executor.setMaxPoolSize(CORE_POOL_SIZE * 2);  // 最大线程数
        executor.setQueueCapacity(100); // 队列大小
        executor.setThreadNamePrefix("AsyncLog-"); // 线程名前缀
        executor.initialize();
        return executor;
    }

    /**
     * 邮件发送异步任务线程池
     */
    @Bean(name = "mailTaskExecutor")
    public Executor mailTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(CORE_POOL_SIZE);
        executor.setMaxPoolSize(CORE_POOL_SIZE * 2);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("AsyncEmail-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}