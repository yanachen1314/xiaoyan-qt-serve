package com.yan.xiaoyanqtserve.tool;

import com.yan.xiaoyanqtserve.common.dto.tool.ToolVerifyResponse;
import com.yan.xiaoyanqtserve.common.exception.BizException;
import com.yan.xiaoyanqtserve.common.utils.RedisUtil;
import com.yan.xiaoyanqtserve.config.custome.ToolAuthPropertiesConfig;
import com.yan.xiaoyanqtserve.service.impl.tool.ToolAuthService;
import com.yan.xiaoyanqtserve.service.impl.tool.ToolJwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.redis.serializer.SerializationException;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ToolAuthServiceTest {

    private RedisUtil redisUtil;
    private ToolJwtService toolJwtService;
    private ToolAuthService toolAuthService;

    @BeforeEach
    void setUp() {
        redisUtil = mock(RedisUtil.class);
        toolJwtService = new ToolJwtService();
        ToolAuthPropertiesConfig properties = new ToolAuthPropertiesConfig();
        properties.setCodeExpireMinutes(5);
        properties.setTokenExpireMinutes(10);
        properties.setTokenUses(1);
        properties.setVerifyFailLimit(5);
        properties.setVerifyLockMinutes(10);
        toolAuthService = new ToolAuthService(redisUtil, toolJwtService, properties);
    }

    @Test
    @DisplayName("保存微信验证码时会写入 Redis 并设置过期时间")
    void saveWechatVerificationCode() {
        toolAuthService.saveWechatVerificationCode("doubao", "openid-1", "123456");

        verify(redisUtil).set(
                eq("tool:wechat:code:doubao:123456"),
                any(String.class),
                eq(5L),
                eq(TimeUnit.MINUTES)
        );
    }

    @Test
    @DisplayName("正确验证码可以换取一次性工具 JWT")
    void verifyCodeAndIssueToken() {
        when(redisUtil.get("tool:wechat:code:doubao:123456"))
                .thenReturn("""
                        {"toolCode":"doubao","openIdHash":"openid-hash","createdAt":1717488000000}
                        """);

        ToolVerifyResponse response = toolAuthService.verifyAndIssueToken(
                "doubao",
                "123456",
                "browser-fingerprint"
        );

        assertNotNull(response.toolToken());
        assertEquals(600, response.expiresIn());
        assertEquals(1, response.remainingUses());
        verify(redisUtil).delete("tool:wechat:code:doubao:123456");

        ArgumentCaptor<String> tokenKeyCaptor = ArgumentCaptor.forClass(String.class);
        verify(redisUtil).set(
                tokenKeyCaptor.capture(),
                any(String.class),
                eq(10L),
                eq(TimeUnit.MINUTES)
        );
        assertTrue(tokenKeyCaptor.getValue().startsWith("tool:token:jti:"));
    }

    @Test
    @DisplayName("Redis 中缺少类型信息的旧验证码不会冒泡为未知运行时异常")
    void redisDeserializeFailureIsHandledAsInvalidCode() {
        when(redisUtil.get("tool:wechat:code:doubao:123456"))
                .thenThrow(new SerializationException("missing type id property '@class'"));

        BizException exception = assertThrows(BizException.class, () ->
                toolAuthService.verifyAndIssueToken("doubao", "123456", "browser-fingerprint"));

        assertTrue(exception.getMessage().contains("验证码"));
    }

    @Test
    @DisplayName("错误验证码会记录设备失败次数")
    void invalidCodeRecordsFailure() {
        BizException exception = assertThrows(BizException.class, () ->
                toolAuthService.verifyAndIssueToken("doubao", "000000", "browser-fingerprint"));

        assertTrue(exception.getMessage().contains("验证码"));
        verify(redisUtil).incr(eq("tool:verify:fail:" + ToolAuthService.sha256("browser-fingerprint")), eq(1L));
    }

    @Test
    @DisplayName("同一个工具 JWT 只能消费一次")
    void consumeTokenOnlyOnce() {
        String deviceIdHash = ToolAuthService.sha256("browser-fingerprint");
        String token = toolJwtService.generateToolToken(
                "doubao",
                deviceIdHash,
                "openid-hash",
                "jti-once",
                Duration.ofMinutes(10)
        );
        when(redisUtil.getAndDelete("tool:token:jti:jti-once"))
                .thenReturn("""
                        {"toolCode":"doubao","deviceIdHash":"%s","openIdHash":"openid-hash","remainingUses":1}
                        """.formatted(deviceIdHash))
                .thenReturn(null);

        toolAuthService.consumeToolToken("doubao", "browser-fingerprint", token);

        assertThrows(BizException.class,
                () -> toolAuthService.consumeToolToken("doubao", "browser-fingerprint", token));
    }

    @Test
    @DisplayName("设备指纹不一致时拒绝消费工具 JWT")
    void rejectDeviceMismatch() {
        String deviceIdHash = ToolAuthService.sha256("browser-fingerprint");
        String token = toolJwtService.generateToolToken(
                "doubao",
                deviceIdHash,
                "openid-hash",
                "jti-device",
                Duration.ofMinutes(10)
        );

        assertThrows(BizException.class,
                () -> toolAuthService.consumeToolToken("doubao", "other-fingerprint", token));
    }
}
