package com.yan.xiaoyanqtserve.service.impl.wechat;

import java.time.Instant;

/**
 * 微信公众号被动回复 XML 构建工具。
 */
public class WechatReplyBuilder {

    private WechatReplyBuilder() {
    }

    /**
     * 构造微信被动回复文本消息 XML。
     *
     * @param toUser 接收方账号，通常为用户 OpenID
     * @param fromUser 发送方账号，通常为公众号原始 ID
     * @param content 回复文本内容
     * @return 微信被动回复文本 XML
     */
    public static String replyText(String toUser, String fromUser, String content) {
        return """
                <xml>
                    <ToUserName><![CDATA[%s]]></ToUserName>
                    <FromUserName><![CDATA[%s]]></FromUserName>
                    <CreateTime>%d</CreateTime>
                    <MsgType><![CDATA[text]]></MsgType>
                    <Content><![CDATA[%s]]></Content>
                </xml>""".formatted(toUser, fromUser, Instant.now().getEpochSecond(), content);
    }
}
