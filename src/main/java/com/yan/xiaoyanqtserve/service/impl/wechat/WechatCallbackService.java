package com.yan.xiaoyanqtserve.service.impl.wechat;

import com.alibaba.fastjson2.JSON;
import com.yan.xiaoyanqtserve.common.Constants;
import com.yan.xiaoyanqtserve.common.dto.wechat.WechatMessage;
import com.yan.xiaoyanqtserve.service.SysConfigService;
import com.yan.xiaoyanqtserve.service.impl.tool.ToolAuthService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;
import java.util.List;

/**
 * 微信公众号消息与事件回调处理服务。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WechatCallbackService {
    private static final String DOUBAO_TOOL_CODE = "doubao";
    private static final String DEFAULT_SUBSCRIBE_REPLY = """
            欢迎关注【云辰小烟】公众号。

            这是小烟GO服务账号，有任何问题请回复菜单关键字或编号，快速获取对应内容。
            [握手][握手][握手]
            =========================

            【快捷菜单】
            👉🏻【1】获取验证码
            用于解锁官网中一系列的免费工具[礼物]
            👉🏻【2】小烟改写工具
            用于文本改写、语句优化、表达润色，帮助降低文本中的AI生成痕迹，使内容更自然、更通顺、更贴近人工写作风格。
            👉🏻【3】人工降重 / 降AI / 格式修改
            了解人工文本润色、重复率优化、AI痕迹优化、论文/文章修改、Word格式调整等服务。
            👉🏻【4】联系我
            获取联系方式、问题反馈与合作咨询方式。
            👉🏻【0】重新查看功能菜单
            """;
    private static final String DEFAULT_MENU_REPLY_1 = "您的回填验证码：{code}";
    private static final String DEFAULT_MENU_REPLY_2 = """
            【小烟改写工具】
            可用于文本改写、语句优化、表达润色，帮助降低文本中的AI生成痕迹，使内容更自然、更通顺、更贴近人工写作风格。
            """;
    private static final String DEFAULT_MENU_REPLY_3 = """
            【人工降重 / 降AI / 格式修改】
            可咨询人工文本润色、重复率优化、AI痕迹优化、论文/文章修改、Word格式调整等服务。
            """;
    private static final String DEFAULT_MENU_REPLY_4 = """
            【联系我】
            如需问题反馈、合作咨询或人工服务，请联系：Yeaokk
            """;
    private static final List<WechatMenuReplyRule> DEFAULT_MENU_REPLY_RULES = List.of(
            new WechatMenuReplyRule(Constants.WECHAT_MENU_REPLY_1_KEY,
                    List.of("1", "验证码", "验证", "解锁", "获取验证码", "豆包"), true),
            new WechatMenuReplyRule(Constants.WECHAT_MENU_REPLY_2_KEY,
                    List.of("2", "小烟改写", "改写工具", "文本改写", "润色"), false),
            new WechatMenuReplyRule(Constants.WECHAT_MENU_REPLY_3_KEY,
                    List.of("3", "人工降重", "降重", "降ai", "降AI", "格式修改", "论文修改"), false),
            new WechatMenuReplyRule(Constants.WECHAT_MENU_REPLY_4_KEY,
                    List.of("4", "联系我", "联系", "客服", "反馈", "合作"), false),
            new WechatMenuReplyRule(Constants.WECHAT_SUBSCRIBE_REPLY_KEY,
                    List.of("0", "菜单", "功能菜单", "帮助", "重新查看"), false)
    );
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

    private String handleTextMessage(WechatMessage message) {
        log.info("收到文本消息: open_id_hash={}, content={}", hashOpenId(message.getFromUserName()), message.getContent());
        String content = message.getContent() == null ? "" : message.getContent().trim();
        if (!StringUtils.hasText(content)) {
            return null;
        }
        for (WechatMenuReplyRule rule : getMenuReplyRules()) {
            if (rule.matches(content)) {
                return buildMenuReply(message, rule);
            }
        }
        return null;
    }

    /**
     * 获取微信公众号关注回复内容，如果未配置则返回默认内容。
     * @return 关注回复内容
     */
    private String getSubscribeReplyContent() {
        return getConfiguredText(Constants.WECHAT_SUBSCRIBE_REPLY_KEY, DEFAULT_SUBSCRIBE_REPLY);
    }

    /**
     * 获取微信公众号配置的回复内容，如果未配置则返回默认内容。
     * @param key 配置项 key
     * @param defaultContent 默认内容
     * @return 配置内容，如果未配置则返回默认内容
     */
    private String getConfiguredText(String key, String defaultContent) {
        try {
            String content = sysConfigService.getValue(key);
            if (StringUtils.hasText(content)) {
                return content;
            }
            log.warn("微信公众号回复配置为空: key={}", key);
        } catch (RuntimeException e) {
            log.warn("读取微信公众号回复配置失败，使用默认文案: key={}, error={}", key, e.getMessage());
        }
        return defaultContent;
    }

    /**
     * 获取微信公众号菜单回复规则。
     * @return 菜单回复规则列表，如果未配置则返回默认规则
     */
    private List<WechatMenuReplyRule> getMenuReplyRules() {
        try {
            String rulesJson = sysConfigService.getValue(Constants.WECHAT_MENU_REPLY_RULES_KEY);
            if (!StringUtils.hasText(rulesJson)) {
                return DEFAULT_MENU_REPLY_RULES;
            }
            List<WechatMenuReplyRule> rules = JSON.parseArray(rulesJson, WechatMenuReplyRule.class);
            if (rules != null && !rules.isEmpty()) {
                return rules;
            }
            log.warn("微信公众号菜单回复规则为空: key={}", Constants.WECHAT_MENU_REPLY_RULES_KEY);
        } catch (RuntimeException e) {
            log.warn("读取微信公众号菜单回复规则失败，使用默认规则: key={}, error={}",
                    Constants.WECHAT_MENU_REPLY_RULES_KEY, e.getMessage());
        }
        return DEFAULT_MENU_REPLY_RULES;
    }

    /**
     * 根据菜单回复规则构建回复内容。
     * @param message 微信消息实体
     * @param rule 菜单回复规则
     * @return 回复内容
     */
    private String buildMenuReply(WechatMessage message, WechatMenuReplyRule rule) {
        String content = getConfiguredText(rule.getReplyKey(), getDefaultReplyContent(rule.getReplyKey()));
        if (rule.isGenerateCode()) {
            String code = generateVerificationCode();
            saveDoubaoCode(message, code);
            content = content.replace("{code}", code);
        }
        return WechatReplyBuilder.replyText(
                message.getFromUserName(),
                message.getToUserName(),
                content
        );
    }

    private String getDefaultReplyContent(String replyKey) {
        return switch (replyKey) {
            case Constants.WECHAT_SUBSCRIBE_REPLY_KEY -> DEFAULT_SUBSCRIBE_REPLY;
            case Constants.WECHAT_MENU_REPLY_1_KEY -> DEFAULT_MENU_REPLY_1;
            case Constants.WECHAT_MENU_REPLY_2_KEY -> DEFAULT_MENU_REPLY_2;
            case Constants.WECHAT_MENU_REPLY_3_KEY -> DEFAULT_MENU_REPLY_3;
            case Constants.WECHAT_MENU_REPLY_4_KEY -> DEFAULT_MENU_REPLY_4;
            default -> "该菜单内容暂未配置，请回复 0 查看功能菜单。";
        };
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

    @Setter
    @Getter
    public static class WechatMenuReplyRule {
        private String replyKey;
        private List<String> keywords;
        private Boolean generateCode;

        public WechatMenuReplyRule() {
        }

        public WechatMenuReplyRule(String replyKey, List<String> keywords, Boolean generateCode) {
            this.replyKey = replyKey;
            this.keywords = keywords;
            this.generateCode = generateCode;
        }

        private boolean isGenerateCode() {
            return Boolean.TRUE.equals(generateCode);
        }

        private boolean matches(String content) {
            if (!StringUtils.hasText(replyKey) || keywords == null || keywords.isEmpty()) {
                return false;
            }
            return keywords.stream()
                    .filter(StringUtils::hasText)
                    .map(String::trim)
                    .anyMatch(content::equals);
        }
    }
}
