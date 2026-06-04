package com.yan.xiaoyanqtserve.tool;

import com.yan.xiaoyanqtserve.common.exception.BizException;
import com.yan.xiaoyanqtserve.common.utils.JwtUtil;
import com.yan.xiaoyanqtserve.service.impl.tool.ToolJwtService;
import com.yan.xiaoyanqtserve.service.impl.tool.ToolTokenClaims;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ToolJwtServiceTest {

    private final ToolJwtService toolJwtService = new ToolJwtService();

    @Test
    @DisplayName("可以生成并解析工具 JWT")
    void generateAndParseToolToken() {
        String token = toolJwtService.generateToolToken(
                "doubao",
                "device-hash",
                "openid-hash",
                "jti-1",
                Duration.ofMinutes(10)
        );

        ToolTokenClaims claims = toolJwtService.parseToolToken(token);

        assertEquals("doubao", claims.toolCode());
        assertEquals("device-hash", claims.deviceIdHash());
        assertEquals("openid-hash", claims.openIdHash());
        assertEquals("jti-1", claims.jti());
    }

    @Test
    @DisplayName("管理员 JWT 不能作为工具令牌使用")
    void rejectAdminJwtToken() {
        String adminToken = new JwtUtil().generateToken("admin");

        BizException exception = assertThrows(BizException.class,
                () -> toolJwtService.parseToolToken(adminToken));

        assertTrue(exception.getMessage().contains("工具令牌"));
    }

    @Test
    @DisplayName("过期工具 JWT 不能通过解析")
    void rejectExpiredToolToken() {
        String token = toolJwtService.generateToolToken(
                "doubao",
                "device-hash",
                "openid-hash",
                "jti-expired",
                Duration.ofSeconds(-1)
        );

        assertThrows(BizException.class, () -> toolJwtService.parseToolToken(token));
    }
}
