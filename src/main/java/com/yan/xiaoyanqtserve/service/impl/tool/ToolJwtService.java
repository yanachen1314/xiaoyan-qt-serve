package com.yan.xiaoyanqtserve.service.impl.tool;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.yan.xiaoyanqtserve.common.enums.BizStatus;
import com.yan.xiaoyanqtserve.common.exception.BizException;
import com.yan.xiaoyanqtserve.common.utils.KeysUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.jackson.io.JacksonSerializer;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 工具 JWT 服务，专门用于站点工具一次性授权令牌。
 */
@Service
public class ToolJwtService {
    private static final String ISSUER = "xiaoyanAi";
    private static final String SUBJECT = "tool-token";
    private static final String AUDIENCE = "tool";
    private static final String CLAIM_TYPE = "typ";
    private static final String CLAIM_TOOL_CODE = "toolCode";
    private static final String CLAIM_DEVICE_ID_HASH = "deviceIdHash";
    private static final String CLAIM_OPEN_ID_HASH = "openIdHash";

    /**
     * 生成带工具声明和 jti 的 JWT。
     *
     * @param toolCode 工具编码
     * @param deviceIdHash 浏览器指纹哈希
     * @param openIdHash 微信 openId 哈希
     * @param jti JWT 唯一编号
     * @param ttl 有效期
     * @return 工具 JWT
     */
    public String generateToolToken(
            String toolCode,
            String deviceIdHash,
            String openIdHash,
            String jti,
            Duration ttl
    ) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_TYPE, SUBJECT);
        claims.put(CLAIM_TOOL_CODE, toolCode);
        claims.put(CLAIM_DEVICE_ID_HASH, deviceIdHash);
        claims.put(CLAIM_OPEN_ID_HASH, openIdHash);

        long now = System.currentTimeMillis();
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        return Jwts.builder()
                .json(new JacksonSerializer<>(objectMapper))
                .issuer(ISSUER)
                .subject(SUBJECT)
                .audience().add(AUDIENCE).and()
                .issuedAt(new Date(now))
                .expiration(new Date(now + ttl.toMillis()))
                .id(jti)
                .claims(claims)
                .signWith(KeysUtil.getPrivateKey(), Jwts.SIG.RS512)
                .compact();
    }

    /**
     * 解析并校验工具 JWT。
     *
     * @param token 工具 JWT
     * @return 工具令牌声明
     */
    public ToolTokenClaims parseToolToken(String token) {
        if (!StringUtils.hasText(token)) {
            throw new BizException(BizStatus.UNAUTHORIZED, "缺少工具令牌");
        }

        Claims claims;
        try {
            claims = Jwts.parser()
                    .verifyWith(KeysUtil.getPublicKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            throw new BizException(BizStatus.UNAUTHORIZED, "工具令牌已过期", e);
        } catch (JwtException | IllegalArgumentException e) {
            throw new BizException(BizStatus.UNAUTHORIZED, "工具令牌无效", e);
        }

        if (!SUBJECT.equals(claims.getSubject()) || !SUBJECT.equals(claims.get(CLAIM_TYPE, String.class))) {
            throw new BizException(BizStatus.UNAUTHORIZED, "工具令牌类型无效");
        }

        String toolCode = claims.get(CLAIM_TOOL_CODE, String.class);
        String deviceIdHash = claims.get(CLAIM_DEVICE_ID_HASH, String.class);
        String openIdHash = claims.get(CLAIM_OPEN_ID_HASH, String.class);
        String jti = claims.getId();
        if (!StringUtils.hasText(toolCode)
                || !StringUtils.hasText(deviceIdHash)
                || !StringUtils.hasText(openIdHash)
                || !StringUtils.hasText(jti)) {
            throw new BizException(BizStatus.UNAUTHORIZED, "工具令牌声明不完整");
        }

        return new ToolTokenClaims(toolCode, deviceIdHash, openIdHash, jti);
    }
}
