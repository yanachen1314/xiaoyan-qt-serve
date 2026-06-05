package com.yan.xiaoyanqtserve.service.impl.wechat;

import com.yan.xiaoyanqtserve.common.Constants;
import com.yan.xiaoyanqtserve.common.dto.wechat.WechatMessage;
import com.yan.xiaoyanqtserve.service.SysConfigService;
import com.yan.xiaoyanqtserve.service.impl.tool.ToolAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;

/**
 * 微信公众号消息与事件回调处理服务。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WechatCallbackService {
    private static final String DOUBAO_TOOL_CODE = "doubao";
    private static final String DEFAULT_SUBSCRIBE_REPLY = """
            欢迎关注云辰小烟公众号，有问题可以私信我！
            
            小烟工具最新版本已经发布，点击下载: <a href="https://pan.baidu.com/s/5Y5SjbcfkQfF6w1Lvvd1LLg#list/path=%2F%E5%B0%8F%E7%83%9F%E5%B7%A5%E5%85%B7">网盘链接</a>
            工具激活卡密购买地址，点击下载：<a href="https://pay.ldxp.cn/shop/xiaoyanai">云辰小烟店铺</a>
            人工降重复、降ai 、格式修改| 联系：Yeaokk
            """;
    private final SecureRandom secureRandom = new SecureRandom();
    private final ToolAuthService toolAuthService;
    private final SysConfigService sysConfigService;

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
            log.info("微信用户取消关注: open_id_hash={}", hashOpenId(message.getFromUserName()));
            return null;
        }

        if (message.isTextMessage()) {
            return handleTextMessage(message);
        }

        if (message.isQrCodeSubscribeEvent()) {
            log.info("收到扫描带参数二维码事件（未关注）: open_id_hash={}, scene={}",
                    hashOpenId(message.getFromUserName()), message.getEventKey());
            return null;
        }

        if (message.isQrCodeScanEvent()) {
            log.info("收到扫描带参数二维码事件（已关注）: open_id_hash={}, scene={}",
                    hashOpenId(message.getFromUserName()), message.getEventKey());
            return null;
        }

        if (message.isEventMessage()) {
            log.info("收到暂未处理的微信事件: event={}", message.getEvent());
            return null;
        }

        log.info("收到暂未处理的微信消息: msgType={}", message.getMsgType());
        return null;
    }

    /**
     * 生成用于关注或关键词回复的 6 位数字验证码。
     *
     * @return 六位数字验证码
     */
    protected String generateVerificationCode() {
        return "%06d".formatted(secureRandom.nextInt(1_000_000));
    }

    private String handleSubscribeEvent(WechatMessage message) {
        log.info("微信用户关注: open_id_hash={}", hashOpenId(message.getFromUserName()));
        return WechatReplyBuilder.replyText(
                message.getFromUserName(),
                message.getToUserName(),
                getSubscribeReplyContent()
        );
    }

    private String getSubscribeReplyContent() {
        try {
            String content = sysConfigService.getValue(Constants.WECHAT_SUBSCRIBE_REPLY_KEY);
            if (StringUtils.hasText(content)) {
                return content;
            }
            log.warn("微信公众号关注回复配置为空: key={}", Constants.WECHAT_SUBSCRIBE_REPLY_KEY);
        } catch (RuntimeException e) {
            log.warn("读取微信公众号关注回复配置失败，使用默认文案: key={}, error={}",
                    Constants.WECHAT_SUBSCRIBE_REPLY_KEY, e.getMessage());
        }
        return DEFAULT_SUBSCRIBE_REPLY;
    }

    private String handleTextMessage(WechatMessage message) {
        log.info("收到文本消息: open_id_hash={}, content={}", hashOpenId(message.getFromUserName()), message.getContent());
        if ("豆包".equals(message.getContent())) {
            String code = generateVerificationCode();
            saveDoubaoCode(message, code);
            return WechatReplyBuilder.replyText(
                    message.getFromUserName(),
                    message.getToUserName(),
                    "您的回填验证码：" + code
            );
        }
        return null;
    }

    private void saveDoubaoCode(WechatMessage message, String code) {
        toolAuthService.saveWechatVerificationCode(DOUBAO_TOOL_CODE, message.getFromUserName(), code);
    }

    private String hashOpenId(String openId) {
        if (openId == null || openId.isBlank()) {
            return "";
        }
        return ToolAuthService.sha256(openId).substring(0, 12);
    }
}
