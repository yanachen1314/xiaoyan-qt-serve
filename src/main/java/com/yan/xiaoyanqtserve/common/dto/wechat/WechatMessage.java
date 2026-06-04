package com.yan.xiaoyanqtserve.common.dto.wechat;

import lombok.Getter;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.Map;

/**
 * 微信公众号回调消息实体，统一承载普通消息、事件消息和扫码事件字段。
 */
@Getter
public class WechatMessage {
    public static final String MSG_TYPE_TEXT = "text";
    public static final String MSG_TYPE_EVENT = "event";
    public static final String EVENT_TYPE_SUBSCRIBE = "subscribe";
    public static final String EVENT_TYPE_UNSUBSCRIBE = "unsubscribe";
    public static final String EVENT_TYPE_SCAN = "SCAN";

    private final Map<String, String> raw;
    private final String toUserName;
    private final String fromUserName;
    private final String createTime;
    private final String msgType;
    private final String event;
    private final String eventKey;
    private final String ticket;
    private final String content;
    private final String msgId;

    public WechatMessage(Map<String, String> raw) {
        this.raw = Collections.unmodifiableMap(raw);
        this.toUserName = raw.getOrDefault("ToUserName", "");
        this.fromUserName = raw.getOrDefault("FromUserName", "");
        this.createTime = raw.getOrDefault("CreateTime", "");
        this.msgType = raw.getOrDefault("MsgType", "");
        this.event = raw.getOrDefault("Event", "");
        this.eventKey = raw.getOrDefault("EventKey", "");
        this.ticket = raw.getOrDefault("Ticket", "");
        this.content = raw.getOrDefault("Content", "");
        this.msgId = raw.getOrDefault("MsgId", "");
    }

    /**
     * 判断当前消息是否为文本消息。
     *
     * @return true 表示文本消息
     */
    public boolean isTextMessage() {
        return MSG_TYPE_TEXT.equals(msgType);
    }

    /**
     * 判断当前消息是否为普通关注事件，不包含带场景值二维码关注事件。
     *
     * @return true 表示普通关注事件
     */
    public boolean isSubscribeEvent() {
        return MSG_TYPE_EVENT.equals(msgType)
                && EVENT_TYPE_SUBSCRIBE.equals(event)
                && !StringUtils.hasText(eventKey);
    }

    /**
     * 判断当前消息是否为取消关注事件。
     *
     * @return true 表示取消关注事件
     */
    public boolean isUnsubscribeEvent() {
        return MSG_TYPE_EVENT.equals(msgType) && EVENT_TYPE_UNSUBSCRIBE.equals(event);
    }

    /**
     * 判断当前消息是否为未关注用户扫描带参数二维码后的关注事件。
     *
     * @return true 表示扫码关注事件
     */
    public boolean isQrCodeSubscribeEvent() {
        return MSG_TYPE_EVENT.equals(msgType)
                && EVENT_TYPE_SUBSCRIBE.equals(event)
                && StringUtils.hasText(eventKey);
    }

    /**
     * 判断当前消息是否为已关注用户扫描带参数二维码事件。
     *
     * @return true 表示扫码事件
     */
    public boolean isQrCodeScanEvent() {
        return MSG_TYPE_EVENT.equals(msgType) && EVENT_TYPE_SCAN.equals(event);
    }

    /**
     * 判断当前消息是否为事件类型消息。
     *
     * @return true 表示事件消息
     */
    public boolean isEventMessage() {
        return MSG_TYPE_EVENT.equals(msgType);
    }
}
