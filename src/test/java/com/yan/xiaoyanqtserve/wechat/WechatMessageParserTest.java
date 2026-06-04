package com.yan.xiaoyanqtserve.wechat;

import com.yan.xiaoyanqtserve.common.dto.wechat.WechatMessage;
import com.yan.xiaoyanqtserve.service.impl.wechat.WechatMessageParser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WechatMessageParserTest {

    private final WechatMessageParser parser = new WechatMessageParser();

    @Test
    @DisplayName("解析文本消息 XML")
    void parseTextMessage() {
        WechatMessage message = parser.parse("""
                <xml>
                    <ToUserName><![CDATA[gh_xiaoyan]]></ToUserName>
                    <FromUserName><![CDATA[openid-1]]></FromUserName>
                    <CreateTime>1710000000</CreateTime>
                    <MsgType><![CDATA[text]]></MsgType>
                    <Content><![CDATA[豆包]]></Content>
                    <MsgId>123</MsgId>
                </xml>
                """);

        assertEquals("gh_xiaoyan", message.getToUserName());
        assertEquals("openid-1", message.getFromUserName());
        assertEquals("text", message.getMsgType());
        assertEquals("豆包", message.getContent());
        assertTrue(message.isTextMessage());
    }

    @Test
    @DisplayName("识别未关注用户扫码后的关注事件")
    void parseQrSubscribeEvent() {
        WechatMessage message = parser.parse("""
                <xml>
                    <ToUserName><![CDATA[gh_xiaoyan]]></ToUserName>
                    <FromUserName><![CDATA[openid-2]]></FromUserName>
                    <CreateTime>1710000000</CreateTime>
                    <MsgType><![CDATA[event]]></MsgType>
                    <Event><![CDATA[subscribe]]></Event>
                    <EventKey><![CDATA[qrscene_1001]]></EventKey>
                    <Ticket><![CDATA[ticket-value]]></Ticket>
                </xml>
                """);

        assertTrue(message.isQrCodeSubscribeEvent());
        assertEquals("qrscene_1001", message.getEventKey());
        assertEquals("ticket-value", message.getTicket());
    }

    @Test
    @DisplayName("空消息体或非法 XML 会抛出解析异常")
    void parseInvalidXml() {
        assertThrows(IllegalArgumentException.class, () -> parser.parse(""));
        assertThrows(IllegalArgumentException.class, () -> parser.parse("<xml>"));
    }
}
