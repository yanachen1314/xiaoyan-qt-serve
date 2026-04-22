package com.yan.xiaoyanqtserve.service.mail;

import com.yan.xiaoyanqtserve.common.exception.BizException;
import com.yan.xiaoyanqtserve.config.MultiMailSenderConfig;
import com.yan.xiaoyanqtserve.entity.MailAccount;
import jakarta.annotation.Resource;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 邮件发送服务（多账号轮询版）
 * @author yanchen
 */
@Slf4j
@Service
public class MailService {

    @Resource
    private MultiMailSenderConfig multiMailSenderConfig;

    @Resource
    private MailAccountService mailAccountService;

    @Resource
    private TemplateService templateService;

    /**
     * 发送验证码邮件
     * @param from 发送者
     * @param to 收件人
     * @param function 验证码功能,eg:"密码重置验证"
     * @param code 验证码
     * @param expireTime 验证码过期时间,单位：分钟
     */
    public void sendVerifyCodeMail(String from, String to, String function, String code, int expireTime) {
        String htmlContent = templateService.generateVerifyCodeTemplate(function, code, expireTime);
        sendHtmlMail(from, to, "验证码通知", htmlContent);
    }

    /**
     * 发送验证码邮件,使用轮询策略选择发送账号
     * @param to 收件人
     * @param function 验证码功能,eg:"密码重置验证"
     * @param code 验证码
     * @param expireTime 验证码过期时间,单位：分钟
     */
    public void sendVerifyCodeMail(String to, String function, String code, int expireTime) {
        String htmlContent = templateService.generateVerifyCodeTemplate(function, code, expireTime);
        sendHtmlMail(null, to, "验证码通知", htmlContent);
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
        sendHtmlMail(null, "2742015729@qq.com", "系统服务邮箱授权码到期提醒", htmlContent);
    }

    /**
     * 生成欢迎邮件模板
     * @param to 收件人
     * @param userName 用户名
     * @param content 欢迎语
     */
    public void sendWelcomeTemplate(String to, String userName, String content) {
        String htmlContent = templateService.generateWelcomeTemplate(userName, content);
        sendHtmlMail(null, to, "欢迎邮件", htmlContent);
    }

    /**
     * 密码重置邮件模板
     * @param to 收件人
     * @param userName 用户名
     */
    public void sendPwdResetTemplate(String to, String userName) {
        String htmlContent = templateService.generatePwdResetTemplate(userName,LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        sendHtmlMail(null, to, "用户密码变更提醒", htmlContent);
    }

    /**
     * 通用的发送HTML邮件方法（支持多账号轮询）
     * @param from 邮件发送者，如果为null则使用轮询策略选择发送账号
     * @param to 收件人
     * @param subject 邮件主题
     * @param htmlContent html格式的邮件内容
     */
    @Async("mailTaskExecutor")
    public void sendHtmlMail(String from, String to, String subject, String htmlContent) {
        LocalDate today = LocalDate.now();
        
        // 如果指定了from，尝试使用指定的发送者
        if (from != null) {
            JavaMailSender sender = multiMailSenderConfig.getMailSender(from);
            if (sender == null) {
                throw new BizException("指定的发送邮箱不可用: " + from);
            }
            sendMailInternal(sender, from, to, subject, htmlContent, today);
            return;
        }
        
        // 使用轮询策略选择发送账号，失败时尝试其他可用账号
        List<MailAccount> availableAccounts = mailAccountService.getAvailableAccounts();
        if (availableAccounts.isEmpty()) {
            throw new BizException("没有可用的邮箱账号");
        }
        
        MailAccount selectedAccount = mailAccountService.selectAccountByRoundRobin();
        String selectedEmail = selectedAccount.getEmailAddress();
        JavaMailSender sender = multiMailSenderConfig.getMailSender(selectedEmail);
        
        if (sender == null) {
            throw new BizException("选中的发送邮箱不可用: " + selectedEmail);
        }
        
        try {
            sendMailInternal(sender, selectedEmail, to, subject, htmlContent, today);
        } catch (BizException e) {
            // 发送失败，标记当前账号并尝试其他账号
            mailAccountService.recordFailure(selectedEmail, today);
            
            // 尝试其他可用账号
            for (MailAccount account : availableAccounts) {
                if (!account.getEmailAddress().equals(selectedEmail)) {
                    JavaMailSender retrySender = multiMailSenderConfig.getMailSender(account.getEmailAddress());
                    if (retrySender != null && !mailAccountService.isAccountOverLimit(account, today)) {
                        try {
                            sendMailInternal(retrySender, account.getEmailAddress(), to, subject, htmlContent, today);
                            return; // 发送成功
                        } catch (Exception retryEx) {
                            mailAccountService.recordFailure(account.getEmailAddress(), today);
                            log.warn("重试邮箱 {} 发送失败: {}", account.getEmailAddress(), retryEx.getMessage());
                        }
                    }
                }
            }
            
            throw new BizException("所有可用邮箱账号发送失败");
        }
    }

    /**
     * 执行实际发送邮件的操作
     * 
     * @param sender JavaMailSender实例
     * @param from 发送者邮箱
     * @param to 收件人邮箱
     * @param subject 邮件主题
     * @param htmlContent html格式的邮件内容
     * @param today 当天日期
     */
    private void sendMailInternal(JavaMailSender sender, String from, String to, String subject, String htmlContent, LocalDate today) {
        try {
            MimeMessage message = sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            sender.send(message);
            
            // 记录发送成功统计
            mailAccountService.recordSuccess(from, today);
            log.debug("邮件发送成功: {} -> {}", from, to);
        } catch (Exception e) {
            log.error("发送邮件异常: {}", e.getMessage());
            if (e.getMessage().contains("550") || e.getMessage().contains("non-existent account")) {
                throw new BizException("请检查收件人邮箱账号是否正确");
            } else if (e.getMessage().contains("Authentication failed") || e.getMessage().contains("invalid authentication")) {
                throw new BizException("请检查系统邮箱账号密码是否正确");
            } else {
                throw new BizException("发送邮件异常");
            }
        }
    }
}
