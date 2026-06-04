package com.yan.xiaoyanqtserve.service.impl.wechat;

import com.yan.xiaoyanqtserve.config.custome.WechatPropertiesConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HexFormat;

/**
 * 微信公众号回调签名服务。
 */
@Service
@RequiredArgsConstructor
public class WechatSignatureService {
    private final WechatPropertiesConfig wechatPropertiesConfig;

    /**
     * 按微信服务器配置规则生成 SHA1 签名。
     *
     * @param timestamp 微信回调 timestamp 参数
     * @param nonce 微信回调 nonce 参数
     * @return 十六进制 SHA1 签名
     */
    public String buildSignature(String timestamp, String nonce) {
        String[] parts = {wechatPropertiesConfig.getToken(), timestamp, nonce};
        Arrays.sort(parts);
        return sha1(String.join("", parts));
    }

    /**
     * 校验微信回调请求中的签名是否可信。
     *
     * @param signature 微信回调 signature 参数
     * @param timestamp 微信回调 timestamp 参数
     * @param nonce 微信回调 nonce 参数
     * @return true 表示签名可信
     */
    public boolean verifySignature(String signature, String timestamp, String nonce) {
        if (!StringUtils.hasText(signature)
                || !StringUtils.hasText(timestamp)
                || !StringUtils.hasText(nonce)) {
            return false;
        }

        byte[] expected = buildSignature(timestamp, nonce).getBytes(StandardCharsets.UTF_8);
        byte[] actual = signature.toLowerCase().getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(expected, actual);
    }

    private String sha1(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] bytes = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("当前 Java 环境不支持 SHA-1 算法", e);
        }
    }
}
