package com.yan.xiaoyanqtserve.wechat;

import com.yan.xiaoyanqtserve.common.dto.wechat.WechatMessage;
import com.yan.xiaoyanqtserve.service.impl.tool.ToolAuthService;
import com.yan.xiaoyanqtserve.service.impl.wechat.WechatCallbackService;
import com.yan.xiaoyanqtserve.service.impl.wechat.WechatMessageParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class WechatCallbackServiceTest {

    private final WechatMessageParser parser = new WechatMessageParser();
    private ToolAuthService toolAuthService;
    private WechatCallbackService service;

    @BeforeEach
    void setUp() {
        toolAuthService = mock(ToolAuthService.class);
        service = new FixedCodeWechatCallbackService(toolAuthService);
    }

    @Test
    @DisplayName("普通关注事件返回关注验证码并保存到工具校验服务")
    void handleSubscribeEvent() {
        WechatMessage message = parser.parse("""
                <xml>
                    <ToUserName><![CDATA[gh_xiaoyan]]></ToUserName>
                    <FromUserName><![CDATA[openid-1]]></FromUserName>
                    <CreateTime>1710000000</CreateTime>
                    <MsgType><![CDATA[event]]></MsgType>
                    <Event><![CDATA[subscribe]]></Event>
                </xml>
                """);

        String reply = service.handleCallback(message);

        assertTrue(reply.contains("<ToUserName><![CDATA[openid-1]]></ToUserName>"));
        assertTrue(reply.contains("<FromUserName><![CDATA[gh_xiaoyan]]></FromUserName>"));
        assertTrue(reply.contains("<Content><![CDATA[感谢关注，验证码：1234]]></Content>"));
        verify(toolAuthService).saveWechatVerificationCode("doubao", "openid-1", "1234");
    }

    @Test
    @DisplayName("文本内容为豆包时返回回填验证码并保存到工具校验服务")
    void handleDoubaoTextMessage() {
        WechatMessage message = parser.parse("""
                <xml>
                    <ToUserName><![CDATA[gh_xiaoyan]]></ToUserName>
                    <FromUserName><![CDATA[openid-1]]></FromUserName>
                    <CreateTime>1710000000</CreateTime>
                    <MsgType><![CDATA[text]]></MsgType>
                    <Content><![CDATA[豆包]]></Content>
                </xml>
                """);

        String reply = service.handleCallback(message);

        assertTrue(reply.contains("<Content><![CDATA[您的回填验证码：1234]]></Content>"));
        verify(toolAuthService).saveWechatVerificationCode("doubao", "openid-1", "1234");
    }

    @Test
    @DisplayName("取消关注、扫码事件和普通文本不返回被动回复")
    void handleNoReplyMessages() {
        assertNull(service.handleCallback(parser.parse("""
                <xml>
                    <ToUserName><![CDATA[gh_xiaoyan]]></ToUserName>
                    <FromUserName><![CDATA[openid-1]]></FromUserName>
                    <CreateTime>1710000000</CreateTime>
                    <MsgType><![CDATA[event]]></MsgType>
                    <Event><![CDATA[unsubscribe]]></Event>
                </xml>
                """)));
        assertNull(service.handleCallback(parser.parse("""
                <xml>
                    <ToUserName><![CDATA[gh_xiaoyan]]></ToUserName>
                    <FromUserName><![CDATA[openid-1]]></FromUserName>
                    <CreateTime>1710000000</CreateTime>
                    <MsgType><![CDATA[event]]></MsgType>
                    <Event><![CDATA[SCAN]]></Event>
                    <EventKey><![CDATA[1001]]></EventKey>
                </xml>
                """)));
        assertNull(service.handleCallback(parser.parse("""
                <xml>
                    <ToUserName><![CDATA[gh_xiaoyan]]></ToUserName>
                    <FromUserName><![CDATA[openid-1]]></FromUserName>
                    <CreateTime>1710000000</CreateTime>
                    <MsgType><![CDATA[text]]></MsgType>
                    <Content><![CDATA[你好]]></Content>
                </xml>
                """)));
    }

    private static class FixedCodeWechatCallbackService extends WechatCallbackService {
        private FixedCodeWechatCallbackService(ToolAuthService toolAuthService) {
            super(toolAuthService);
        }

        @Override
        protected String generateVerificationCode() {
            return "1234";
        }
    }
}
