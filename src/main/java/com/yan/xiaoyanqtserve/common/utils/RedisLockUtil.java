package com.yan.xiaoyanqtserve.common.utils;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * Redis 分布式锁工具类
 * @author yanchen
 * @since 2025/9/1 17:30
 */
@Slf4j
@Component
public class RedisLockUtil {
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    // ThreadLocal 存储当前线程的锁值
    private static final ThreadLocal<String> lockValueThreadLocal = new ThreadLocal<>();

    /**
     * 尝试获取分布式锁（安全版本）
     *
     * @param lockKey       锁的 key
     * @param expireSeconds 过期时间（秒）
     * @return true 表示获取锁成功，false 表示获取锁失败
     */
    public boolean lock(String lockKey, int expireSeconds) {
        try {
            String lockValue = IdUtil.getUUIDSimple();
            Boolean success = redisTemplate.opsForValue().setIfAbsent(
                    lockKey, lockValue, expireSeconds, TimeUnit.SECONDS);

            if (success != null && success) {
                // 将锁值存储到 ThreadLocal 中，供 unlock 使用
                lockValueThreadLocal.set(lockValue);
                return true;
            }
            return false;
        } catch (Exception e) {
            log.info("Redis 锁获取失败，key: {}", lockKey, e);
            return false;
        }
    }

    /**
     * 尝试获取分布式锁（默认30秒过期）
     *
     * @param lockKey 锁的 key
     * @return true 表示获取锁成功，false 表示获取锁失败
     */
    public boolean lock(String lockKey) {
        return lock(lockKey, 30);
    }

    /**
     * 安全释放分布式锁
     *
     * @param lockKey 锁的 key
     */
    public void unlock(String lockKey) {
        try {
            String lockValue = lockValueThreadLocal.get();
            if (lockValue == null) {
                return;
            }

            // Lua 脚本：只有当锁的值匹配时才删除
            String luaScript =
                    "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                            "   return redis.call('del', KEYS[1]) " +
                            "else " +
                            "   return 0 " +
                            "end";

            DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
            redisScript.setScriptText(luaScript);
            redisScript.setResultType(Long.class);

            redisTemplate.execute(redisScript, Collections.singletonList(lockKey), lockValue);

            // 清理 ThreadLocal
            lockValueThreadLocal.remove();
        } catch (Exception e) {
            log.info("Redis 锁释放失败，key: {}", lockKey, e);
            // 清理 ThreadLocal 避免内存泄漏
            lockValueThreadLocal.remove();
        }
    }

    /**
     * 带重试机制的获取锁方法
     *
     * @param lockKey 锁的 key
     * @param expireSeconds 过期时间（秒）
     * @param maxRetries 最大重试次数
     * @param retryInterval 重试间隔（毫秒）
     * @return true 表示获取锁成功，false 表示获取锁失败
     */
    public boolean lockWithRetry(String lockKey, int expireSeconds, int maxRetries, int retryInterval) {
        int retryCount = 0;
        while (retryCount < maxRetries) {
            if (lock(lockKey, expireSeconds)) {
                return true;
            }
            retryCount++;
            if (retryCount < maxRetries) {
                try {
                    Thread.sleep(retryInterval);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return false;
                }
            }
        }
        return false;
    }

}
