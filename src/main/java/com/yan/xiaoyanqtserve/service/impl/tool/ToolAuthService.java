package com.yan.xiaoyanqtserve.service.impl.tool;

import com.yan.xiaoyanqtserve.common.dto.tool.ToolTokenState;
import com.yan.xiaoyanqtserve.common.dto.tool.ToolVerificationCode;
import com.yan.xiaoyanqtserve.common.dto.tool.ToolVerifyResponse;
import com.yan.xiaoyanqtserve.common.enums.BizStatus;
import com.yan.xiaoyanqtserve.common.exception.BizException;
import com.yan.xiaoyanqtserve.common.utils.RedisUtil;
import com.yan.xiaoyanqtserve.config.custome.ToolAuthPropertiesConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 站点工具使用校验服务，负责微信验证码、工具 JWT 与一次性消费状态。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ToolAuthService {
    private static final String CODE_KEY_PREFIX = "tool:wechat:code:";
    private static final String TOKEN_JTI_KEY_PREFIX = "tool:token:jti:";
    private static final String VERIFY_FAIL_KEY_PREFIX = "tool:verify:fail:";

    private final RedisUtil redisUtil;
    private final ToolJwtService toolJwtService;
    private final ToolAuthPropertiesConfig properties;

    /**
     * 保存微信公众号返回给用户的工具验证码。
     *
     * @param toolCode 工具编码
     * @param openId 微信 openId
     * @param code 验证码
     */
    public void saveWechatVerificationCode(String toolCode, String openId, String code) {
        requireText(toolCode, "工具编码不能为空");
        requireText(openId, "微信用户标识不能为空");
        requireText(code, "验证码不能为空");

        ToolVerificationCode verificationCode = new ToolVerificationCode(
                toolCode,
                sha256(openId),
                System.currentTimeMillis()
        );
        redisUtil.set(
                codeKey(toolCode, code),
                verificationCode,
                properties.getCodeExpireMinutes(),
                TimeUnit.MINUTES
        );
    }

    /**
     * 使用验证码换取一次性工具 JWT。
     *
     * @param toolCode 工具编码
     * @param code 验证码
     * @param deviceId 浏览器指纹
     * @return 工具 JWT 响应
     */
    public ToolVerifyResponse verifyAndIssueToken(String toolCode, String code, String deviceId) {
        requireText(toolCode, "工具编码不能为空");
        requireText(code, "验证码不能为空");
        requireText(deviceId, "缺少浏览器指纹");

        String deviceIdHash = sha256(deviceId);
        ensureDeviceNotLocked(deviceIdHash);

        Object cached = redisUtil.get(codeKey(toolCode, code));
        if (!(cached instanceof ToolVerificationCode verificationCode)
                || !toolCode.equals(verificationCode.toolCode())) {
            recordVerifyFailure(deviceIdHash);
            throw new BizException(BizStatus.BAD_REQUEST, "验证码无效或已过期");
        }

        redisUtil.delete(codeKey(toolCode, code));
        String jti = UUID.randomUUID().toString();
        int tokenUses = Math.max(1, properties.getTokenUses());
        Duration ttl = Duration.ofMinutes(properties.getTokenExpireMinutes());
        String token = toolJwtService.generateToolToken(
                toolCode,
                deviceIdHash,
                verificationCode.openIdHash(),
                jti,
                ttl
        );

        redisUtil.set(
                tokenKey(jti),
                new ToolTokenState(toolCode, deviceIdHash, verificationCode.openIdHash(), tokenUses),
                properties.getTokenExpireMinutes(),
                TimeUnit.MINUTES
        );
        redisUtil.delete(failKey(deviceIdHash));

        return new ToolVerifyResponse(token, ttl.toSeconds(), tokenUses);
    }

    /**
     * 校验并消费一次性工具 JWT。
     *
     * @param expectedToolCode 期望访问的工具编码
     * @param deviceId 浏览器指纹
     * @param token 工具 JWT
     */
    public void consumeToolToken(String expectedToolCode, String deviceId, String token) {
        requireText(expectedToolCode, "工具编码不能为空");
        requireText(deviceId, "缺少浏览器指纹");

        ToolTokenClaims claims = toolJwtService.parseToolToken(token);
        String deviceIdHash = sha256(deviceId);
        if (!expectedToolCode.equals(claims.toolCode())) {
            throw new BizException(BizStatus.UNAUTHORIZED, "工具令牌不属于当前工具");
        }
        if (!deviceIdHash.equals(claims.deviceIdHash())) {
            throw new BizException(BizStatus.UNAUTHORIZED, "工具令牌与当前设备不匹配");
        }

        Object stateObject = redisUtil.getAndDelete(tokenKey(claims.jti()));
        if (!(stateObject instanceof ToolTokenState state)
                || state.remainingUses() <= 0
                || !expectedToolCode.equals(state.toolCode())
                || !deviceIdHash.equals(state.deviceIdHash())
                || !claims.openIdHash().equals(state.openIdHash())) {
            throw new BizException(BizStatus.UNAUTHORIZED, "工具令牌无效或已使用");
        }
    }

    /**
     * SHA-256 哈希工具，用于脱敏 openId 和浏览器指纹。
     *
     * @param value 原始值
     * @return 十六进制哈希
     */
    public static String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("当前运行环境不支持 SHA-256", e);
        }
    }

    private void ensureDeviceNotLocked(String deviceIdHash) {
        Object countObject = redisUtil.get(failKey(deviceIdHash));
        long failCount = toLong(countObject);
        if (failCount >= properties.getVerifyFailLimit()) {
            throw new BizException(BizStatus.FORBIDDEN, "验证失败次数过多，请稍后再试");
        }
    }

    private void recordVerifyFailure(String deviceIdHash) {
        Long failCount = redisUtil.incr(failKey(deviceIdHash), 1L);
        if (failCount != null && failCount == 1L) {
            redisUtil.expire(failKey(deviceIdHash), properties.getVerifyLockMinutes(), TimeUnit.MINUTES);
        }
    }

    private long toLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value instanceof String text && StringUtils.hasText(text)) {
            return Long.parseLong(text);
        }
        return 0L;
    }

    private void requireText(String value, String message) {
        if (!StringUtils.hasText(value)) {
            throw new BizException(BizStatus.BAD_REQUEST, message);
        }
    }

    private String codeKey(String toolCode, String code) {
        return CODE_KEY_PREFIX + toolCode + ":" + code;
    }

    private String tokenKey(String jti) {
        return TOKEN_JTI_KEY_PREFIX + jti;
    }

    private String failKey(String deviceIdHash) {
        return VERIFY_FAIL_KEY_PREFIX + deviceIdHash;
    }
}
