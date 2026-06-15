package com.yan.xiaoyanqtserve.service.impl.wechat;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONField;
import com.yan.xiaoyanqtserve.common.Constants;
import com.yan.xiaoyanqtserve.common.dto.wechat.WechatMessage;
import com.yan.xiaoyanqtserve.service.SysConfigService;
import com.yan.xiaoyanqtserve.service.impl.tool.ToolAuthService;
import com.yan.xiaoyanqtserve.service.impl.tool.ToolCodeService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;
import java.util.Comparator;
import java.util.List;

/**
 * 微信公众号消息与事件回调处理服务。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WechatCallbackService {
    private static final String CONFIG_MISSING_REPLY = "抱歉，当前自动回复内容暂未配置完整，暂时无法为您自动处理。请联系人工客服反馈问题，我们会尽快为您处理。";
    private final SecureRandom secureRandom = new SecureRandom();

    private final ToolAuthService toolAuthService;
    private final SysConfigService sysConfigService;
    private final ToolCodeService toolCodeService;

    /**
     * 处理微信回调消息，并返回需要被动回复给微信服务器的 XML。
     *
     * @param message 微信回调消息实体
     * @return XML 回复；返回 null 时表示只响应 success，不给用户发送消息
     */
    public String handleCallback(WechatMessage message) {
        // 处理普通扫码关注事件
        if (message.isSubscribeEvent()) {
            return handleSubscribeEvent(message);
        }

        // 处理用户取消关注事件
        if (message.isUnsubscribeEvent()) {
            log.info("微信用户取消关注: open_id_hash={}", hashOpenId(message.getFromUserName()));
            return null;
        }

        // 处理文本消息
        if (message.isTextMessage()) {
            return handleTextMessage(message);
        }

        // 处理扫描带参数二维码事件（未关注）
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

    private String handleSubscribeEvent(WechatMessage message) {
        log.info("微信用户关注: open_id_hash={}", hashOpenId(message.getFromUserName()));
        return WechatReplyBuilder.replyText(
                message.getFromUserName(),
                message.getToUserName(),
                getSubscribeReplyContent()
        );
    }

    /**
     * 处理微信文本消息，并返回需要被动回复给微信服务器的 XML。<br>
     * 根据不同菜单关键字回复不同内容。
     * @param message 微信消息实体
     * @return XML 回复；返回 null 时表示只响应 success，不给用户发送消息
     */
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
        return buildFallbackReply(message);
    }

    /**
     * 获取微信公众号关注回复内容，如果未配置则返回统一提示。
     * @return 关注回复内容
     */
    private String getSubscribeReplyContent() {
        String content = getConfiguredText(Constants.WECHAT_SUBSCRIBE_REPLY_KEY);
        return StringUtils.hasText(content) ? content : CONFIG_MISSING_REPLY;
    }

    /**
     * 获取微信公众号配置的回复内容。
     * @param key 配置项 key
     * @return 配置内容，如果未配置则返回 null
     */
    private String getConfiguredText(String key) {
        try {
            String content = sysConfigService.getValue(key);
            if (StringUtils.hasText(content)) {
                return content;
            }
            log.warn("微信公众号回复配置为空: key={}", key);
        } catch (RuntimeException e) {
            log.warn("读取微信公众号回复配置失败: key={}, error={}", key, e.getMessage());
        }
        return null;
    }

    /**
     * 获取微信公众号菜单回复规则。
     * @return 菜单回复规则列表，如果未配置则返回空列表
     */
    private List<WechatMenuReplyRule> getMenuReplyRules() {
        try {
            String rulesJson = sysConfigService.getValue(Constants.WECHAT_MENU_REPLY_RULES_KEY);
            if (!StringUtils.hasText(rulesJson)) {
                log.warn("微信公众号菜单回复规则未配置: key={}", Constants.WECHAT_MENU_REPLY_RULES_KEY);
                return List.of();
            }
            List<WechatMenuReplyRule> rules = JSON.parseArray(rulesJson, WechatMenuReplyRule.class);
            if (rules != null && !rules.isEmpty()) {
                return sortMenuReplyRules(rules);
            }
            log.warn("微信公众号菜单回复规则为空: key={}", Constants.WECHAT_MENU_REPLY_RULES_KEY);
        } catch (RuntimeException e) {
            log.warn("读取微信公众号菜单回复规则失败: key={}, error={}",
                    Constants.WECHAT_MENU_REPLY_RULES_KEY, e.getMessage());
        }
        return List.of();
    }

    private List<WechatMenuReplyRule> sortMenuReplyRules(List<WechatMenuReplyRule> rules) {
        return rules.stream()
                .sorted(Comparator.comparingInt(WechatMenuReplyRule::priorityValue))
                .toList();
    }

    /**
     * 根据菜单回复规则构建回复内容。
     * @param message 微信消息实体
     * @param rule 菜单回复规则
     * @return 回复内容
     */
    private String buildMenuReply(WechatMessage message, WechatMenuReplyRule rule) {
        String content = getConfiguredText(rule.getReplyKey());
        if (!StringUtils.hasText(content)) {
            return buildFallbackReply(message);
        }
        if (rule.isGenerateCode()) {
            String code = generateVerificationCode();
            saveToolCode(message, code, toolCodeService.requireValidCode(rule.getToolCode()));
            content = content.replace("{code}", code);
        }
        return WechatReplyBuilder.replyText(
                message.getFromUserName(),
                message.getToUserName(),
                content
        );
    }

    private String buildFallbackReply(WechatMessage message) {
        return WechatReplyBuilder.replyText(
                message.getFromUserName(),
                message.getToUserName(),
                CONFIG_MISSING_REPLY
        );
    }

    /**
     * 生成用于关注或关键词回复的 6 位数字验证码。
     *
     * @return 六位数字验证码
     */
    protected String generateVerificationCode() {
        return "%06d".formatted(secureRandom.nextInt(1_000_000));
    }

    /**
     * 保存微信验证码。
     *
     * @param message 微信消息实体
     * @param code 验证码
     */
    private void saveToolCode(WechatMessage message, String code, String toolCode) {
        toolAuthService.saveWechatVerificationCode(toolCode, message.getFromUserName(), code);
    }

    /**
     * 对微信 openId 进行哈希处理，保护用户隐私。
     *
     * @param openId 微信 openId
     * @return 哈希后的 openId
     */
    private String hashOpenId(String openId) {
        if (openId == null || openId.isBlank()) {
            return "";
        }
        return ToolAuthService.sha256(openId).substring(0, 12);
    }

    /**
     * 微信菜单回复规则实体
     */
    @Setter
    @Getter
    public static class WechatMenuReplyRule {
        /**
         * 待回复内容的config_key
         */
        private String replyKey;
        /**
         * 匹配关键字
         */
        private List<String> keywords;
        /**
         * 是否生成验证码（r1使用）
         */
        private Boolean generateCode = false;
        /**
         * Tool_code configured by wechat.tool.item.
         */
        @JSONField(name = "tool_code")
        private String toolCode;
        /**
         * 用来控制多条规则的匹配顺序，数值越小越先匹配
         */
        private Integer priority = Integer.MAX_VALUE;

        public WechatMenuReplyRule() {
        }

        public WechatMenuReplyRule(String replyKey, List<String> keywords, Boolean generateCode) {
            this(replyKey, keywords, generateCode, null, null);
        }

        public WechatMenuReplyRule(String replyKey, List<String> keywords, Boolean generateCode,
                                   String toolCode, Integer priority) {
            this.replyKey = replyKey;
            this.keywords = keywords;
            this.generateCode = generateCode != null && generateCode;
            this.toolCode = toolCode;
            this.priority = priority == null ? Integer.MAX_VALUE : priority;
        }

        private boolean isGenerateCode() {
            return Boolean.TRUE.equals(generateCode);
        }

        private int priorityValue() {
            return priority == null ? Integer.MAX_VALUE : priority;
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
