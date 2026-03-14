package com.yan.xiaoyanqtserve.aop;

import com.yan.xiaoyanqtserve.common.Constants;
import com.yan.xiaoyanqtserve.common.annotaions.RateLimiter;
import com.yan.xiaoyanqtserve.common.utils.SecurityContextUtil;
import com.yan.xiaoyanqtserve.entity.KeyInfo;
import com.yan.xiaoyanqtserve.common.exception.BizException;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Instant;
import java.util.Collections;
import java.util.Objects;

@Aspect
@Component
public class RateLimiterAspect {

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private SecurityContextUtil securityContextUtil;

    @Around("@annotation(rateLimiter)")
    public Object around(ProceedingJoinPoint point, RateLimiter rateLimiter) throws Throwable {
        HttpServletRequest request = null;
        try {
            request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        } catch (IllegalStateException e) {
            // 非Web请求环境，比如定时任务等
            throw new BizException("无法获取请求上下文");
        }

        // 获取密钥值
        String keyValue = getKeyValue(request, rateLimiter);
        String methodName = point.getSignature().getName();
        String key = Constants.RATE_LIMIT_PREFIX + methodName + ":" + keyValue;
        if (!rateLimiter.key().isEmpty()) {
            key = Constants.RATE_LIMIT_PREFIX + rateLimiter.key() + ":" + keyValue;
        }

        boolean allowed;
        if (rateLimiter.rateMode() == 1) {
            // 固定时间窗口
            allowed = allowRequestFixed(key, rateLimiter.time(), rateLimiter.count());
        } else {
            // 滑动时间窗口
            allowed = allowRequestSliding(key, rateLimiter.time(), rateLimiter.count());
        }

        if (!allowed) {
            throw new BizException(rateLimiter.message());
        }

        return point.proceed();
    }

    private String getKeyValue(HttpServletRequest request, RateLimiter rateLimiter) {
        return switch (rateLimiter.limitBy()) {
            case IP -> getClientIpAddress(request);
            case API_KEY -> getCurrentApiKey(); // 从 SecurityContext 获取 API Key
        };
    }

    private String getCurrentApiKey() {
        KeyInfo apiKeyUser = securityContextUtil.getApiKeyUser();
        if(securityContextUtil.isApiKeyUser() && Objects.nonNull(apiKeyUser)){
            return apiKeyUser.getKeyValue();
        }else{
            throw new BizException("密钥不存在");
        }
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        return request.getRemoteAddr();
    }

    /**
     * 固定时间窗口限流
     */
    private boolean allowRequestFixed(String key, int time, int count) {
        String luaScript = "local current = redis.call('GET', KEYS[1]) " +
                "if current and tonumber(current) >= tonumber(ARGV[2]) then " +
                "   return 0 " +
                "end " +
                "if current then " +
                "   current = redis.call('INCR', KEYS[1]) " +
                "else " +
                "   current = redis.call('INCR', KEYS[1]) " +
                "   redis.call('EXPIRE', KEYS[1], ARGV[1]) " +
                "end " +
                "return 1";

        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptText(luaScript);
        redisScript.setResultType(Long.class);

        Long result = stringRedisTemplate.execute(
                redisScript,
                Collections.singletonList(key),
                String.valueOf(time),
                String.valueOf(count));

        return result == 1;
    }

    /**
     * 滑动时间窗口限流
     */
    private boolean allowRequestSliding(String key, int time, int count) {
        long nowMillis = Instant.now().toEpochMilli();
        long windowMillis = time * 1000L;
        long minScore = nowMillis - windowMillis;

        String luaScript =
                // 删除窗口外的请求记录
                "redis.call('ZREMRANGEBYSCORE', KEYS[1], 0, ARGV[1]) " +
                        // 获取当前窗口内的请求数
                        "local current = redis.call('ZCARD', KEYS[1]) " +
                        // 判断是否超过限制
                        "if tonumber(current) >= tonumber(ARGV[3]) then " +
                        "   return 0 " +
                        "end " +
                        // 记录当前请求
                        "redis.call('ZADD', KEYS[1], ARGV[2], ARGV[2]) " +
                        // 设置合理的过期时间（窗口时间+缓冲时间）
                        "redis.call('EXPIRE', KEYS[1], ARGV[4]) " +
                        "return 1";

        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptText(luaScript);
        redisScript.setResultType(Long.class);

        Long result = stringRedisTemplate.execute(
                redisScript,
                Collections.singletonList(key),
                String.valueOf(minScore), // ARGV[1]: 删除小于这个时间的记录
                String.valueOf(nowMillis), // ARGV[2]: 当前时间戳（作为score和member）
                String.valueOf(count), // ARGV[3]: 限制次数
                String.valueOf(time + 10) // ARGV[4]: 过期时间（秒）
        );

        return result == 1;
    }

}
