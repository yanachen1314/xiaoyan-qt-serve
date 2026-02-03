package com.yan.xiaoyanqtserve.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.yan.xiaoyanqtserve.common.enums.BizStatus;
import com.yan.xiaoyanqtserve.exception.BizException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.jackson.io.JacksonSerializer;
import io.jsonwebtoken.security.SecurityException;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JwtUtil {

    private static final long JWT_EXPIRE_TIME = 1000 * 60 * 60 * 10; // 10小时

    /**
     * 生成 JWT
     *
     * @param customerId 客户ID
     * @param jwtVer     JWT 版本
     * @return JWT 字符串
     */
    public static String generateToken(String customerId, Integer jwtVer) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("customerId", customerId);
        claims.put("jwtVer", jwtVer);

        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule());
        return Jwts.builder()
                .json(new JacksonSerializer<>(objectMapper))
                .issuer("xiaoyanAi") // 签发者
                .subject("jwtToken") //  主题
                .audience().add("user").and() // 受众
                .issuedAt(new Date()) // 签发时间
                .expiration(new Date(System.currentTimeMillis() + JWT_EXPIRE_TIME)) // 过期时间
                // .notBefore(notBefore) //a java.util.Date
                // .id(UUID.randomUUID().toString()) //just an example id

                .claims(claims)
                .signWith(KeysUtil.getPrivateKey(), Jwts.SIG.RS512)

                .compact();
    }

    /**
     * 获取用户ID
     *
     * @param token JWT令牌
     * @return 用户ID，String
     */
    public static String getCustomerId(String token) {
        return parserToken(token).get("customerId", String.class);
    }


    /**
     * 获取Token版本
     *
     * @param token JWT令牌
     * @return 用户信息， Object
     */
    public static int getJwtVer(String token) {
        return parserToken(token).get("jwtVer", Integer.class);
    }

    /**
     * 解析 JWT 的 Claims
     *
     * @param token JWT 字符串
     * @return JWT Token中的 Claims 信息
     */
    public static Claims parserToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(KeysUtil.getPublicKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (SecurityException e) {
            // 签名不匹配
            throw new BizException(BizStatus.UNAUTHORIZED, "签名不匹配");
        } catch (MalformedJwtException e) {
            // Token 格式错误
            throw new BizException(BizStatus.UNAUTHORIZED, "Token 格式错误");
        } catch (ExpiredJwtException e) {
            // Token 过期
            throw new BizException(BizStatus.UNAUTHORIZED, "Token 过期");
        } catch (UnsupportedJwtException e) {
            // 不支持的 Token 类型
            throw new BizException(BizStatus.UNAUTHORIZED, "不支持的 Token 类型");
        } catch (IllegalArgumentException e) {
            // Token 为空或非法
            throw new BizException(BizStatus.UNAUTHORIZED, "Token 为空或非法");
        }
    }

}