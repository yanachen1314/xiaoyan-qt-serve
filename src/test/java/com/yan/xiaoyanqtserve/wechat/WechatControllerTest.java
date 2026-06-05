package com.yan.xiaoyanqtserve.wechat;

import com.yan.xiaoyanqtserve.config.custome.WechatPropertiesConfig;
import com.yan.xiaoyanqtserve.controller.wechat.WechatController;
import com.yan.xiaoyanqtserve.service.impl.wechat.WechatCallbackService;
import com.yan.xiaoyanqtserve.service.impl.wechat.WechatCryptoService;
import com.yan.xiaoyanqtserve.service.impl.wechat.WechatMessageParser;
import com.yan.xiaoyanqtserve.service.impl.wechat.WechatSignatureService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class WechatControllerTest {

    private MockMvc mockMvc;
    private WechatSignatureService signatureService;
    private WechatCryptoService cryptoService;

    @BeforeEach
    void setUp() {
        WechatPropertiesConfig properties = new WechatPropertiesConfig();
        properties.setToken("asdhjksfdgdfg");
        properties.setAppId("wx-test-appid");
        properties.setEncodingAesKey("abcdefghijklmnopqrstuvwxyz0123456789ABCDEFG");
        signatureService = new WechatSignatureService(properties);
        cryptoService = new WechatCryptoService(properties);
        mockMvc = MockMvcBuilders.standaloneSetup(new WechatController(
                signatureService,
                cryptoService,
                new WechatMessageParser(),
                new FixedCodeWechatCallbackService()
        )).build();
    }

    @Test
    @DisplayName("GET 回调校验成功时返回 echostr 明文")
    void verifyCallbackSuccess() throws Exception {
        String signature = signatureService.buildSignature("1234567890", "nonce-value");

        mockMvc.perform(get("/api/wechat/callback")
                        .param("signature", signature)
                        .param("timestamp", "1234567890")
                        .param("nonce", "nonce-value")
                        .param("echostr", "hello-wechat"))
                .andExpect(status().isOk())
                .andExpect(content().string("hello-wechat"));
    }

    @Test
    @DisplayName("GET 加密回调校验成功时返回解密后的 echostr")
    void verifyEncryptedCallbackSuccess() throws Exception {
        String encryptedEcho = cryptoService.encrypt("hello-encrypted-wechat");
        String msgSignature = signatureService.buildMessageSignature(encryptedEcho, "1234567890", "nonce-value");

        mockMvc.perform(get("/api/wechat/callback")
                        .param("encrypt_type", "aes")
                        .param("msg_signature", msgSignature)
                        .param("timestamp", "1234567890")
                        .param("nonce", "nonce-value")
                        .param("echostr", encryptedEcho))
                .andExpect(status().isOk())
                .andExpect(content().string("hello-encrypted-wechat"));
    }

    @Test
    @DisplayName("GET 回调缺少参数时返回 400")
    void verifyCallbackMissingParams() throws Exception {
        mockMvc.perform(get("/api/wechat/callback"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("缺少微信回调验证参数"));
    }

    @Test
    @DisplayName("POST 回调签名通过且需要回复时返回 XML")
    void receiveCallbackReplyXml() throws Exception {
        String signature = signatureService.buildSignature("1234567890", "nonce-value");

        mockMvc.perform(post("/api/wechat/callback")
                        .param("signature", signature)
                        .param("timestamp", "1234567890")
                        .param("nonce", "nonce-value")
                        .contentType("application/xml;charset=UTF-8")
                        .content("""
                                <xml>
                                    <ToUserName><![CDATA[gh_xiaoyan]]></ToUserName>
                                    <FromUserName><![CDATA[openid-1]]></FromUserName>
                                    <CreateTime>1710000000</CreateTime>
                                    <MsgType><![CDATA[text]]></MsgType>
                                    <Content><![CDATA[豆包]]></Content>
                                </xml>
                                """.getBytes(StandardCharsets.UTF_8)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/xml"))
                .andExpect(content().string(containsString("您的回填验证码：1234")));
    }

    @Test
    @DisplayName("POST 回调不需要回复时返回 success")
    void receiveCallbackPlainSuccess() throws Exception {
        String signature = signatureService.buildSignature("1234567890", "nonce-value");

        mockMvc.perform(post("/api/wechat/callback")
                        .param("signature", signature)
                        .param("timestamp", "1234567890")
                        .param("nonce", "nonce-value")
                        .contentType("application/xml;charset=UTF-8")
                        .content("""
                                <xml>
                                    <ToUserName><![CDATA[gh_xiaoyan]]></ToUserName>
                                    <FromUserName><![CDATA[openid-1]]></FromUserName>
                                    <CreateTime>1710000000</CreateTime>
                                    <MsgType><![CDATA[text]]></MsgType>
                                    <Content><![CDATA[你好]]></Content>
                                </xml>
                                """.getBytes(StandardCharsets.UTF_8)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/plain"))
                .andExpect(content().string("success"));
    }

    @Test
    @DisplayName("POST 加密回调会解密消息并加密被动回复")
    void receiveEncryptedCallbackReplyXml() throws Exception {
        String encryptedBody = cryptoService.buildEncryptedRequestXml("""
                <xml>
                    <ToUserName><![CDATA[gh_xiaoyan]]></ToUserName>
                    <FromUserName><![CDATA[openid-1]]></FromUserName>
                    <CreateTime>1710000000</CreateTime>
                    <MsgType><![CDATA[text]]></MsgType>
                    <Content><![CDATA[豆包]]></Content>
                </xml>
                """);
        String encrypt = cryptoService.extractEncrypt(encryptedBody);
        String msgSignature = signatureService.buildMessageSignature(encrypt, "1234567890", "nonce-value");

        String responseXml = mockMvc.perform(post("/api/wechat/callback")
                        .param("encrypt_type", "aes")
                        .param("msg_signature", msgSignature)
                        .param("timestamp", "1234567890")
                        .param("nonce", "nonce-value")
                        .contentType("application/xml;charset=UTF-8")
                        .content(encryptedBody.getBytes(StandardCharsets.UTF_8)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/xml"))
                .andExpect(content().string(org.hamcrest.Matchers.not(containsString("您的回填验证码"))))
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        String replyEncrypt = cryptoService.extractEncrypt(responseXml);
        String plainReplyXml = cryptoService.decrypt(replyEncrypt);
        org.junit.jupiter.api.Assertions.assertTrue(plainReplyXml.contains("您的回填验证码：1234"));
    }

    @Test
    @DisplayName("POST 加密回调缺少 msg_signature 时返回 400")
    void receiveEncryptedCallbackMissingMsgSignature() throws Exception {
        String encryptedBody = cryptoService.buildEncryptedRequestXml("""
                <xml>
                    <ToUserName><![CDATA[gh_xiaoyan]]></ToUserName>
                    <FromUserName><![CDATA[openid-1]]></FromUserName>
                    <MsgType><![CDATA[text]]></MsgType>
                    <Content><![CDATA[豆包]]></Content>
                </xml>
                """);

        mockMvc.perform(post("/api/wechat/callback")
                        .param("encrypt_type", "aes")
                        .param("timestamp", "1234567890")
                        .param("nonce", "nonce-value")
                        .contentType("application/xml;charset=UTF-8")
                        .content(encryptedBody.getBytes(StandardCharsets.UTF_8)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("缺少微信加密回调签名参数"));
    }

    private static class FixedCodeWechatCallbackService extends WechatCallbackService {
        private FixedCodeWechatCallbackService() {
            super(
                    org.mockito.Mockito.mock(com.yan.xiaoyanqtserve.service.impl.tool.ToolAuthService.class),
                    org.mockito.Mockito.mock(com.yan.xiaoyanqtserve.service.SysConfigService.class)
            );
        }

        @Override
        protected String generateVerificationCode() {
            return "1234";
        }
    }
}
