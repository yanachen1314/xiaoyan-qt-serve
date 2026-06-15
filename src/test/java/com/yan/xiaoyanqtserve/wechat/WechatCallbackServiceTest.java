package com.yan.xiaoyanqtserve.wechat;

import com.yan.xiaoyanqtserve.common.Constants;
import com.yan.xiaoyanqtserve.common.dto.wechat.WechatMessage;
import com.yan.xiaoyanqtserve.service.SysConfigService;
import com.yan.xiaoyanqtserve.service.impl.tool.ToolAuthService;
import com.yan.xiaoyanqtserve.service.impl.tool.ToolCodeService;
import com.yan.xiaoyanqtserve.service.impl.wechat.WechatCallbackService;
import com.yan.xiaoyanqtserve.service.impl.wechat.WechatMessageParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.anyString;

class WechatCallbackServiceTest {

    private final WechatMessageParser parser = new WechatMessageParser();
    private ToolAuthService toolAuthService;
    private SysConfigService sysConfigService;
    private ToolCodeService toolCodeService;
    private WechatCallbackService service;

    @BeforeEach
    void setUp() {
        toolAuthService = mock(ToolAuthService.class);
        sysConfigService = mock(SysConfigService.class);
        toolCodeService = mock(ToolCodeService.class);
        when(toolCodeService.requireValidCode(anyString())).thenAnswer(invocation ->
                invocation.getArgument(0, String.class).trim().toLowerCase());
        service = new FixedCodeWechatCallbackService(toolAuthService, sysConfigService, toolCodeService);
    }

    @Test
    @DisplayName("普通关注事件优先返回系统配置的关注回复")
    void handleSubscribeEvent() {
        when(sysConfigService.getValue(Constants.WECHAT_SUBSCRIBE_REPLY_KEY)).thenReturn("custom subscribe reply");
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
        assertTrue(reply.contains("<Content><![CDATA[custom subscribe reply]]></Content>"));
        verify(sysConfigService).getValue(Constants.WECHAT_SUBSCRIBE_REPLY_KEY);
        verifyNoInteractions(toolAuthService);
    }

    @Test
    @DisplayName("普通关注事件配置缺失时返回客服提示")
    void handleSubscribeEventFallbackDefaultReply() {
        when(sysConfigService.getValue(Constants.WECHAT_SUBSCRIBE_REPLY_KEY)).thenThrow(new RuntimeException("missing"));
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

        assertTrue(reply.contains("自动回复内容暂未配置完整"));
        assertTrue(reply.contains("请联系人工客服反馈问题"));
        verifyNoInteractions(toolAuthService);
    }

    @Test
    @DisplayName("文本内容为豆包时返回回填验证码并保存到工具校验服务")
    void handleDoubaoTextMessage() {
        when(sysConfigService.getValue(Constants.WECHAT_MENU_REPLY_RULES_KEY)).thenReturn("""
                [
                  {"replyKey":"wechat.menu.reply.1-1","keywords":["豆包"],"generateCode":true,"tool_code":"doubao"}
                ]
                """);
        when(sysConfigService.getValue(Constants.WECHAT_MENU_REPLY_1_CODE_KEY)).thenReturn("您的回填验证码：{code}");
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
    @DisplayName("文本内容命中菜单关键字任意一项时返回对应配置内容")
    void handleMenuKeywordReply() {
        when(sysConfigService.getValue(Constants.WECHAT_MENU_REPLY_RULES_KEY)).thenReturn("""
                [
                  {"replyKey":"wechat.menu.reply.1","keywords":["1","验证码","验证","解锁"],"generateCode":false}
                ]
                """);
        when(sysConfigService.getValue(Constants.WECHAT_MENU_REPLY_1_KEY)).thenReturn("输入以下关键字获取对应工具的验证码");
        WechatMessage message = textMessage("解锁");

        String reply = service.handleCallback(message);

        assertTrue(reply.contains("<Content><![CDATA[输入以下关键字获取对应工具的验证码]]></Content>"));
        verifyNoInteractions(toolAuthService);
    }

    @Test
    @DisplayName("验证码规则会按 tool_code 保存到对应工具 Key")
    void handleMenuKeywordReplyWithToolCode() {
        when(sysConfigService.getValue(Constants.WECHAT_MENU_REPLY_RULES_KEY)).thenReturn("""
                [
                  {"replyKey":"wechat.menu.reply.1-1","keywords":["AI小说"],"generateCode":true,"tool_code":"novel"}
                ]
                """);
        when(sysConfigService.getValue(Constants.WECHAT_MENU_REPLY_1_CODE_KEY)).thenReturn("您的验证码是：{code}");
        WechatMessage message = textMessage("AI小说");

        String reply = service.handleCallback(message);

        assertTrue(reply.contains("<Content><![CDATA[您的验证码是：1234]]></Content>"));
        verify(toolAuthService).saveWechatVerificationCode("novel", "openid-1", "1234");
    }

    @Test
    @DisplayName("验证码规则可以使用独立 replyKey 读取不同工具模板")
    void handleCodeRuleWithCustomReplyKey() {
        when(sysConfigService.getValue(Constants.WECHAT_MENU_REPLY_RULES_KEY)).thenReturn("""
                [
                  {"replyKey":"wechat.menu.reply.1-2","keywords":["AI小说"],"generateCode":true,"tool_code":"novel"}
                ]
                """);
        when(sysConfigService.getValue("wechat.menu.reply.1-2")).thenReturn("AI小说验证码：{code}");
        WechatMessage message = textMessage("AI小说");

        String reply = service.handleCallback(message);

        assertTrue(reply.contains("<Content><![CDATA[AI小说验证码：1234]]></Content>"));
        verify(toolAuthService).saveWechatVerificationCode("novel", "openid-1", "1234");
    }

    @Test
    @DisplayName("菜单规则未配置时返回客服提示")
    void missingMenuRulesReturnsFallbackReply() {
        WechatMessage message = textMessage("AI小说");

        String reply = service.handleCallback(message);

        assertTrue(reply.contains("自动回复内容暂未配置完整"));
        assertTrue(reply.contains("请联系人工客服反馈问题"));
        verifyNoInteractions(toolAuthService);
    }

    @Test
    @DisplayName("命中验证码规则但回复文案未配置时不保存验证码")
    void matchedCodeRuleWithoutReplyContentDoesNotSaveCode() {
        when(sysConfigService.getValue(Constants.WECHAT_MENU_REPLY_RULES_KEY)).thenReturn("""
                [
                  {"replyKey":"wechat.menu.reply.1-1","keywords":["豆包"],"generateCode":true,"tool_code":"doubao"}
                ]
                """);
        WechatMessage message = textMessage("豆包");

        String reply = service.handleCallback(message);

        assertTrue(reply.contains("自动回复内容暂未配置完整"));
        verifyNoInteractions(toolAuthService);
    }

    @Test
    @DisplayName("文本内容为0时重新返回功能菜单")
    void handleMenuZeroReply() {
        when(sysConfigService.getValue(Constants.WECHAT_MENU_REPLY_RULES_KEY)).thenReturn("""
                [
                  {"replyKey":"wechat.subscribe.reply","keywords":["0","菜单","功能菜单"]}
                ]
                """);
        when(sysConfigService.getValue(Constants.WECHAT_SUBSCRIBE_REPLY_KEY)).thenReturn("功能菜单内容");
        WechatMessage message = textMessage("0");

        String reply = service.handleCallback(message);

        assertTrue(reply.contains("<Content><![CDATA[功能菜单内容]]></Content>"));
        verifyNoInteractions(toolAuthService);
    }

    @Test
    @DisplayName("文本内容未命中菜单关键字时返回客服提示")
    void handleUnknownTextWithoutMenuReply() {
        when(sysConfigService.getValue(Constants.WECHAT_MENU_REPLY_RULES_KEY)).thenReturn("""
                [
                  {"replyKey":"wechat.menu.reply.1","keywords":["1","验证码","验证","解锁"],"generateCode":true}
                ]
                """);
        WechatMessage message = textMessage("hello");

        String reply = service.handleCallback(message);

        assertTrue(reply.contains("自动回复内容暂未配置完整"));
        verifyNoInteractions(toolAuthService);
    }

    @Test
    @DisplayName("取消关注和扫码事件不返回被动回复")
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
    }

    private static class FixedCodeWechatCallbackService extends WechatCallbackService {
        private FixedCodeWechatCallbackService(ToolAuthService toolAuthService, SysConfigService sysConfigService,
                                               ToolCodeService toolCodeService) {
            super(toolAuthService, sysConfigService, toolCodeService);
        }

        @Override
        protected String generateVerificationCode() {
            return "1234";
        }
    }

    private WechatMessage textMessage(String content) {
        return parser.parse("""
                <xml>
                    <ToUserName><![CDATA[gh_xiaoyan]]></ToUserName>
                    <FromUserName><![CDATA[openid-1]]></FromUserName>
                    <CreateTime>1710000000</CreateTime>
                    <MsgType><![CDATA[text]]></MsgType>
                    <Content><![CDATA[%s]]></Content>
                </xml>
                """.formatted(content));
    }
}
