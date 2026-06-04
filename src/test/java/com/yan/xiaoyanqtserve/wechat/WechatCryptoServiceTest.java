package com.yan.xiaoyanqtserve.wechat;

import com.yan.xiaoyanqtserve.config.custome.WechatPropertiesConfig;
import com.yan.xiaoyanqtserve.service.impl.wechat.WechatCryptoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WechatCryptoServiceTest {

    @Test
    @DisplayName("加密后可以解密回原始 XML")
    void encryptAndDecrypt() {
        WechatCryptoService service = new WechatCryptoService(properties());
        String plainXml = """
                <xml>
                    <ToUserName><![CDATA[gh_xiaoyan]]></ToUserName>
                    <FromUserName><![CDATA[openid-1]]></FromUserName>
                    <MsgType><![CDATA[text]]></MsgType>
                    <Content><![CDATA[豆包]]></Content>
                </xml>
                """;

        String encrypt = service.encrypt(plainXml);

        assertTrue(encrypt.length() > 0);
        assertEquals(plainXml, service.decrypt(encrypt));
    }

    @Test
    @DisplayName("解密时会校验明文末尾 AppID")
    void decryptRejectsWrongAppId() {
        WechatCryptoService source = new WechatCryptoService(properties());
        String encrypt = source.encrypt("<xml><MsgType><![CDATA[text]]></MsgType></xml>");

        WechatPropertiesConfig wrong = properties();
        wrong.setAppId("wx-other-appid");
        WechatCryptoService target = new WechatCryptoService(wrong);

        assertThrows(IllegalArgumentException.class, () -> target.decrypt(encrypt));
    }

    @Test
    @DisplayName("构造密文被动回复 XML")
    void buildEncryptedReplyXml() {
        WechatCryptoService service = new WechatCryptoService(properties());

        String replyXml = service.buildEncryptedReplyXml(
                "<xml><Content><![CDATA[您的回填验证码：1234]]></Content></xml>",
                "1234567890",
                "nonce-value"
        );

        String encrypt = service.extractEncrypt(replyXml);
        assertTrue(replyXml.contains("<MsgSignature><![CDATA["));
        assertEquals(
                "<xml><Content><![CDATA[您的回填验证码：1234]]></Content></xml>",
                service.decrypt(encrypt)
        );
    }

    private WechatPropertiesConfig properties() {
        WechatPropertiesConfig properties = new WechatPropertiesConfig();
        properties.setToken("asdhjksfdgdfg");
        properties.setAppId("wx-test-appid");
        properties.setEncodingAesKey("abcdefghijklmnopqrstuvwxyz0123456789ABCDEFG");
        return properties;
    }
}
