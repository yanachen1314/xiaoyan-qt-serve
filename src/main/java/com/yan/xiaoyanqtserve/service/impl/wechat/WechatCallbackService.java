package com.yan.xiaoyanqtserve.service.impl.wechat;

import com.yan.xiaoyanqtserve.common.dto.wechat.WechatMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

/**
 * 微信公众号消息与事件回调处理服务。
 */
@Slf4j
@Service
public class WechatCallbackService {
    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * 处理微信回调消息，并返回需要被动回复给微信服务器的 XML。
     *
     * @param message 微信回调消息实体
     * @return XML 回复；返回 null 时表示只响应 success，不给用户发送消息
     */
    public String handleCallback(WechatMessage message) {
        if (message.isSubscribeEvent()) {
            return handleSubscribeEvent(message);
        }

        if (message.isUnsubscribeEvent()) {
            log.info("微信用户取消关注: open_id={}", message.getFromUserName());
            return null;
        }

        if (message.isTextMessage()) {
            return handleTextMessage(message);
        }

        if (message.isQrCodeSubscribeEvent()) {
            log.info("收到扫描带参数二维码事件（未关注）: open_id={}, scene={}",
                    message.getFromUserName(), message.getEventKey());
            return null;
        }

        if (message.isQrCodeScanEvent()) {
            log.info("收到扫描带参数二维码事件（已关注）: open_id={}, scene={}",
                    message.getFromUserName(), message.getEventKey());
            return null;
        }

        if (message.isEventMessage()) {
            log.info("收到暂未处理的微信事件实体: event={}", message.getEvent());
            return null;
        }

        log.info("收到暂未处理的微信消息实体: msgType={}", message.getMsgType());
        return null;
    }

    /**
     * 生成用于关注或关键词回复的 4 位数字验证码。
     *
     * @return 四位数字验证码
     */
    protected String generateVerificationCode() {
        return "%04d".formatted(secureRandom.nextInt(10000));
    }

    private String handleSubscribeEvent(WechatMessage message) {
        String code = generateVerificationCode();
        log.info("微信用户关注: open_id={}, code={}", message.getFromUserName(), code);
        return WechatReplyBuilder.replyText(
                message.getFromUserName(),
                message.getToUserName(),
                "感谢关注，验证码：" + code
        );
    }

    private String handleTextMessage(WechatMessage message) {
        log.info("收到文本消息: open_id={}, content={}", message.getFromUserName(), message.getContent());
        if ("豆包".equals(message.getContent())) {
            String code = generateVerificationCode();
            return WechatReplyBuilder.replyText(
                    message.getFromUserName(),
                    message.getToUserName(),
                    "您的回填验证码：" + code
            );
        }
        return null;
    }
}
