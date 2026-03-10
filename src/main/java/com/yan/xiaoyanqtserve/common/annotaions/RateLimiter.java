package com.yan.xiaoyanqtserve.common.annotaions;

import com.yan.xiaoyanqtserve.common.enums.RateLimiterType;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimiter {
    /**
     * 限流key
     */
    String key() default "";
    
    /**
     * 限流时间窗口，单位秒
     */
    int time() default 1;
    
    /**
     * 时间窗口内最大请求次数
     */
    int count() default 1;
    
    /**
     * 限流提示语
     */
    String message() default "请求过于频繁，请稍后再试";

    /**
     * 限流模式 <p>
     * 滑动窗口（0）：
     * <ul>
     *     <li>1秒内只能请求 1 次</li>
     *     <li>任何连续的 1 秒时间段内请求次数超过限制都会被拦截（不受时间边界影响）</li>
     * </ul>
     * <P>
     * 固定窗口（1）：
     * <ul>
     * <li>每个自然秒内只能请求 1 次</li>
     * <li>在窗口边界可能会“钻空子”，比如 0.9s 和 1.1s 之间可以请求两次</li>
     * </ul>
     */
    int rateMode() default 0;

    /**
     * 限流类型 <p>
     * 1：IP <p>
     * 2：API Key <p>
     */
    RateLimiterType limitBy() default RateLimiterType.IP;
}