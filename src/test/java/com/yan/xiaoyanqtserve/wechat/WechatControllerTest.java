package com.yan.xiaoyanqtserve.wechat;

import com.yan.xiaoyanqtserve.config.custome.WechatPropertiesConfig;
import com.yan.xiaoyanqtserve.controller.wechat.WechatController;
import com.yan.xiaoyanqtserve.service.impl.wechat.WechatCallbackService;
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

    @BeforeEach
    void setUp() {
        WechatPropertiesConfig properties = new WechatPropertiesConfig();
        properties.setToken("asdhjksfdgdfg");
        signatureService = new WechatSignatureService(properties);
        mockMvc = MockMvcBuilders.standaloneSetup(new WechatController(
                signatureService,
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

    private static class FixedCodeWechatCallbackService extends WechatCallbackService {
        @Override
        protected String generateVerificationCode() {
            return "1234";
        }
    }
}
