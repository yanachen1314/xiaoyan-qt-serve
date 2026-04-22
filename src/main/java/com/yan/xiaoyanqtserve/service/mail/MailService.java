package com.yan.xiaoyanqtserve.service.mail;

import com.yan.xiaoyanqtserve.common.exception.BizException;
import jakarta.annotation.Resource;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author yanchen
 */
@Slf4j
@Service
public class MailService {
    // 系统邮箱
    @Value("${spring.mail.username}")
    private String sysMail;

    @Resource
    JavaMailSender javaMailSender;

    @Resource
    private TemplateService templateService;

    /**
     * 发送验证码邮件
     * @param function 验证码功能,eg:"密码重置验证"
     * @param from 发送者
     * @param to 收件人
     * @param code 验证码
     * @param expireTime 验证码过期时间,单位：分钟
     */
    public void sendVerifyCodeMail(String from, String to, String function, String code, int expireTime) {
        String htmlContent = templateService.generateVerifyCodeTemplate(function, code, expireTime);
        sendHtmlMail(from, to, "验证码通知", htmlContent);
    }

    /**
     * 发送验证码邮件,默认为系统邮箱
     * @param function 验证码功能,eg:"密码重置验证"
     * @param to 收件人
     * @param code 验证码
     * @param expireTime 验证码过期时间,单位：分钟
     */
    public void sendVerifyCodeMail(String to, String function, String code, int expireTime) {
        sendVerifyCodeMail(sysMail, to, function, code, expireTime);
    }

    /**
     * 系统服务邮箱授权码到期提醒
     * @param activeDate 授权码启用时间
     * @param expiryDate 授权码到期时间
     * @param now 当前时间
     * @param daysUntilExpiry 授权码到期时间与当前时间之间的天数,剩余天数
     */
    public void sendNeteaseAuthCodeMail(LocalDate activeDate,
                                        LocalDate expiryDate,
                                        LocalDate now,
                                        long daysUntilExpiry) {
        String htmlContent = templateService.generateNeteaseAuthCodeTemplate(activeDate, expiryDate, now, daysUntilExpiry);
        sendHtmlMail(sysMail, "2742015729@qq.com", "系统服务邮箱授权码到期提醒", htmlContent);
    }

    /**
     * 生成欢迎邮件模板
     * @param to 收件人
     * @param userName 用户名
     * @param content 欢迎语
     */
    public void sendWelcomeTemplate(String to, String userName, String content) {
        String htmlContent = templateService.generateWelcomeTemplate(userName, content);
        sendHtmlMail(sysMail, to, "欢迎邮件", htmlContent);
    }

    /**
     * 密码重置邮件模板
     * @param to 收件人
     * @param userName 用户名
     */
    public void sendPwdResetTemplate(String to, String userName) {
        String htmlContent = templateService.generatePwdResetTemplate(userName,LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        sendHtmlMail(sysMail, to, "用户密码变更提醒", htmlContent);
    }

    /**
     * 通用的发送HTML邮件方法
     * @param from 邮件发送者
     * @param to 收件人
     * @param subject 邮件主题
     * @param htmlContent html格式的邮件内容
     */
    @Async("mailTaskExecutor")
    public void sendHtmlMail(String from, String to, String subject, String htmlContent) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            javaMailSender.send(message);
        } catch (Exception e) {
            // log.error("发送邮件异常：{}", e.getMessage(), e);
            log.error("发送邮件异常：{}", e.getMessage());
            if (e.getMessage().contains("550") || e.getMessage().contains("non-existent account")) {
                throw new BizException("请检查收件人邮箱账号是否正确");
            }else if (e.getMessage().contains("Authentication failed") || e.getMessage().contains("invalid authentication")) {
                throw new BizException("请检查系统邮箱账号密码是否正确");
            }else {
                throw new BizException("发送邮件异常");
            }
        }
    }
}
