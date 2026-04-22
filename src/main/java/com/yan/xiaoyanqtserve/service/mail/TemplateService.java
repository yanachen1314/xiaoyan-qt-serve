package com.yan.xiaoyanqtserve.service.mail;


import com.yan.xiaoyanqtserve.common.Constants;
import com.yan.xiaoyanqtserve.common.enums.MailTemplateEnum;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDate;
import java.util.Map;

/**
 * 邮件模板服务
 */
@Service
public class TemplateService {

    // 邮件模版路径：resources/templates/mail/*
    private static final String TEMPLATE_PATH = "templates/mail/";

    @Resource
    private TemplateEngine templateEngine;

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
     * 网易邮箱授权码重置邮件模板
     * 
     * @return HTML字符串
     */
    public String generateNeteaseAuthCodeTemplate(
            LocalDate activeDate,
            LocalDate expiryDate,
            LocalDate now,
            long daysUntilExpiry) {
        Context context = new Context();
        context.setVariable("title", "系统服务邮箱授权码到期提醒");
        context.setVariable("activeDate", activeDate);
        context.setVariable("expiryDate", expiryDate);
        context.setVariable("now", now);
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
        context.setVariable("website", Constants.WEB_DOMAIN);

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
        context.setVariable("loginUrl", Constants.WEB_DOMAIN+"/login");

        return templateEngine.process(TEMPLATE_PATH + MailTemplateEnum.PWD_RESET.getName(), context);
    }
}