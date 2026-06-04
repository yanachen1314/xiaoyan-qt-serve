package com.yan.xiaoyanqtserve.wechat;

import com.yan.xiaoyanqtserve.config.custome.WechatPropertiesConfig;
import com.yan.xiaoyanqtserve.service.impl.wechat.WechatSignatureService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WechatSignatureServiceTest {

    @Test
    @DisplayName("按微信公众号规则生成 SHA1 签名")
    void buildSignature() {
        WechatSignatureService service = new WechatSignatureService(properties("asdhjksfdgdfg"));

        String signature = service.buildSignature("1234567890", "nonce-value");

        assertEquals("a0f70f0c3aa881829b5a3c1f7100998faadb1dc5", signature);
    }

    @Test
    @DisplayName("签名一致时校验通过")
    void verifySignatureSuccess() {
        WechatSignatureService service = new WechatSignatureService(properties("asdhjksfdgdfg"));
        String signature = service.buildSignature("1234567890", "nonce-value");

        assertTrue(service.verifySignature(signature, "1234567890", "nonce-value"));
    }

    @Test
    @DisplayName("缺少签名参数或签名不一致时校验失败")
    void verifySignatureFailure() {
        WechatSignatureService service = new WechatSignatureService(properties("asdhjksfdgdfg"));

        assertFalse(service.verifySignature("", "1234567890", "nonce-value"));
        assertFalse(service.verifySignature("bad-signature", "1234567890", "nonce-value"));
    }

    private WechatPropertiesConfig properties(String token) {
        WechatPropertiesConfig properties = new WechatPropertiesConfig();
        properties.setToken(token);
        return properties;
    }
}
