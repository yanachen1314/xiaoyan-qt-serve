package com.yan.xiaoyanqtserve.service.impl.mail;

import com.yan.xiaoyanqtserve.common.enums.EmailServiceProvider;
import com.yan.xiaoyanqtserve.common.enums.MailTemplateEnum;
import com.yan.xiaoyanqtserve.config.custome.WebInfoPropertiesConfig;
import com.yan.xiaoyanqtserve.entity.MailAccount;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 邮件模板服务
 */
@Service
public class TemplateService {

    // 邮件模版路径：resources/templates/mail/*
    private static final String TEMPLATE_PATH = "mail/";

    @Resource
    private TemplateEngine templateEngine;
    @Resource
    private WebInfoPropertiesConfig webInfoPropertiesConfig;

    /**
     * 根据模板名称和参数生成HTML内容
     * 
     * @param templateName 模板名称
     * @param variables    模板参数,Map<String, Object>
     * @return HTML字符串
     */
    public String generateHtmlContent(MailTemplateEnum templateName, Map<String, Object> variables) {
        Context context = new Context();
        context.setVariables(variables);
        return templateEngine.process(TEMPLATE_PATH + templateName.getName(), context);
    }

    /**
     * 生成验证码邮件模板
     * 
     * @param module    主题（该验证码用于哪个功能）
     * @param code      验证码
     * @param validTime 验证码有效期(单位：分钟)
     * @return HTML字符串
     */
    public String generateVerifyCodeTemplate(String module, String code, int validTime) {
        Context context = new Context();
        context.setVariable("title", "验证码通知");
        context.setVariable("module", module);
        context.setVariable("code", code);
        context.setVariable("validTime", validTime);

        return templateEngine.process(TEMPLATE_PATH + MailTemplateEnum.VERIFY_CODE.getName(), context);
    }

    /**
     * 系统邮箱授权码重置邮件模板
     * 
     * @return HTML字符串
     */
    public String generateAuthExpiryWarningMailTemplate(
            MailAccount mailAccount,
            EmailServiceProvider emailServiceProvider,
            LocalDate activeDate,
            LocalDate expiryDate,
            long daysUntilExpiry) {
        Context context = new Context();
        context.setVariable("title", "系统服务邮箱授权码到期提醒");
        context.setVariable("mailAccount", mailAccount);
        context.setVariable("emailServiceProvider", emailServiceProvider.getDisplayName());
        context.setVariable("activeDate", activeDate);
        context.setVariable("expiryDate", expiryDate);
        context.setVariable("now", LocalDate.now());
        context.setVariable("daysUntilExpiry", daysUntilExpiry);

        return templateEngine.process(TEMPLATE_PATH + MailTemplateEnum.NETEASE_AUTH_CODE_RESET.getName(), context);
    }

    /**
     * 生成欢迎邮件模板
     * 
     * @param userName 用户名
     * @param content  欢迎语
     * @return HTML字符串
     */
    public String generateWelcomeTemplate(String userName, String content) {
        Context context = new Context();
        context.setVariable("title", "欢迎邮件");
        context.setVariable("userName", userName);
        context.setVariable("content", content);
        context.setVariable("website", webInfoPropertiesConfig.getApiSite());

        return templateEngine.process(TEMPLATE_PATH + MailTemplateEnum.WELCOME.getName(), context);
    }

    /**
     * 密码变更提醒邮件模板
     * 
     * @param userName   用户名
     * @param changeTime 密码变更时间
     * @return HTML字符串
     */
    public String generatePwdResetTemplate(String userName, String changeTime) {
        Context context = new Context();
        context.setVariable("title", "用户密码变更提醒");
        context.setVariable("userName", userName);
        context.setVariable("changeTime", changeTime);
        context.setVariable("loginUrl", webInfoPropertiesConfig.getApiSite()+"/api/login");

        return templateEngine.process(TEMPLATE_PATH + MailTemplateEnum.PWD_RESET.getName(), context);
    }

    /**
     * 生成免费授权码邮件模板
     *
     * @param to    收件人
     * @param license 授权码/卡密
     * @return HTML字符串
     */
    public String generateFreeLicenseTemplate(String to, String license) {
        Context context = new Context();
        context.setVariable("title", "免费卡密");
        context.setVariable("to", to);
        context.setVariable("license", license);
        context.setVariable("website", webInfoPropertiesConfig.getApiSite());
        context.setVariable("shopUrl", webInfoPropertiesConfig.getLdxpUrl());

        return templateEngine.process(TEMPLATE_PATH + MailTemplateEnum.FREE_LICENSE.getName(), context);
    }

    /**
     * 生成邮箱验证链接邮件模板
     *
     * @param to 收件人
     * @param verifyUrl 验证链接URL
     * @param expireTime 验证链接有效期（分钟）
     * @return HTML字符串
     */
    public String generateEmailVerifyLinkTemplate(String to, String verifyUrl, String expireTime) {
        Context context = new Context();
        context.setVariable("title", "邮箱验证");
        context.setVariable("to", to);
        context.setVariable("verifyUrl", verifyUrl);
        context.setVariable("expireTime", expireTime);
        context.setVariable("website", webInfoPropertiesConfig.getApiSite());
        context.setVariable("shopUrl", webInfoPropertiesConfig.getLdxpUrl());

        return templateEngine.process(TEMPLATE_PATH + MailTemplateEnum.EMAIL_VERIFY_LINK.getName(), context);
    }

    /**
     * 生成邮箱验证结果-成功页面模板
     *
     * @param email 验证成功的邮箱地址
     * @return HTML字符串
     */
    public String generateVerifyEmailSuccessPage(String email) {
        Context context = new Context();
        context.setVariable("title", "验证成功 - 小烟改写工具");
        context.setVariable("email", email);
        context.setVariable("verifyTime", LocalDateTime.now().toString());

        return templateEngine.process(TEMPLATE_PATH + MailTemplateEnum.VERIFY_EMAIL_SUCCESS.getName(), context);
    }

    /**
     * 生成邮箱验证结果-失败页面模板
     *
     * @param title   错误标题
     * @param message 错误详情
     * @return HTML字符串
     */
    public String generateVerifyEmailFailurePage(String title, String message) {
        Context context = new Context();
        context.setVariable("title", title);
        context.setVariable("message", message);
        context.setVariable("websiteUrl", webInfoPropertiesConfig.getApiSite());
        context.setVariable("errorTime", LocalDateTime.now().toString());

        return templateEngine.process(TEMPLATE_PATH + MailTemplateEnum.VERIFY_EMAIL_FAILURE.getName(), context);
    }
}