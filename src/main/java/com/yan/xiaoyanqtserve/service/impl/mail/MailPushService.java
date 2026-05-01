package com.yan.xiaoyanqtserve.service.impl.mail;

import com.yan.xiaoyanqtserve.common.Constants;
import com.yan.xiaoyanqtserve.common.dto.MailRecordDto;
import com.yan.xiaoyanqtserve.common.enums.EmailServiceProvider;
import com.yan.xiaoyanqtserve.common.exception.BizException;
import com.yan.xiaoyanqtserve.config.MultiMailSenderConfig;
import com.yan.xiaoyanqtserve.entity.MailAccount;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 邮件推送服务（多账号轮询版）
 * @author yanchen
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MailPushService {

    private final TemplateService templateService;
    private final MultiMailSenderConfig multiMailSenderConfig;
    private final MailAccountManagerService mailAccountManagerService;
    private final MailApplyManagerService mailApplyManagerService;

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
     * @param mailAccount 邮箱账号
     * @param emailServiceProvider 邮箱服务商
     * @param activeDate 授权码启用时间
     * @param expiryDate 授权码到期时间
     * @param daysUntilExpiry 授权码到期时间与当前时间之间的天数,剩余天数
     */
    public void sendAuthExpiryWarningMail(MailAccount mailAccount, EmailServiceProvider emailServiceProvider,
                                          LocalDate activeDate, LocalDate expiryDate, long daysUntilExpiry) {
        String htmlContent = templateService.generateAuthExpiryWarningMailTemplate(
                mailAccount, emailServiceProvider, activeDate, expiryDate, daysUntilExpiry);
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
     * 免费卡密邮件模板
     * @param to 收件人
     * @param license 卡密
     * @return CompletableFuture，包含实际使用的发送账号邮箱地址
     */
    public CompletableFuture<String> sendFreeLicenseEmail(String to, String license) {
        String htmlContent = templateService.generateFreeLicenseTemplate(to,license);
        return sendHtmlMailWithAccount(null, to, "【小烟改写工具】免费卡密赠送", htmlContent);
    }

    /**
     * 邮箱验证链接邮件模板
     * @param to 收件人
     * @param verifyUrl 验证链接URL
     * @return CompletableFuture，包含实际使用的发送账号邮箱地址
     */
    public CompletableFuture<String> sendEmailVerifyLink(String to, String verifyUrl) {
        String htmlContent = templateService.generateEmailVerifyLinkTemplate(to, verifyUrl, String.valueOf(Constants.EMAIL_VERIFY_EXPIRE_MINUTES));
        return sendHtmlMailWithAccount(null, to, "【小烟改写工具】邮箱验证", htmlContent);
    }

    /**
     * 异步发送HTML邮件方法（支持多账号轮询）
     * @param from 邮件发送者，如果为null则使用轮询策略选择发送账号
     * @param to 收件人
     * @param subject 邮件主题
     * @param htmlContent html格式的邮件内容
     */
    @Async("mailTaskExecutor")
    public void sendHtmlMail(String from, String to, String subject, String htmlContent) {
        doSendHtmlMail(from, to, subject, htmlContent);
    }

    /**
     * 异步发送HTML邮件方法（支持多账号轮询，返回实际使用的发送账号）
     * @param from 邮件发送者，如果为null则使用轮询策略选择发送账号
     * @param to 收件人
     * @param subject 邮件主题
     * @param htmlContent html格式的邮件内容
     * @return CompletableFuture，包含实际使用的发送账号邮箱地址
     */
    @Async("mailTaskExecutor")
    public CompletableFuture<String> sendHtmlMailWithAccount(String from, String to, String subject, String htmlContent) {
        return CompletableFuture.completedFuture(doSendHtmlMail(from, to, subject, htmlContent));
    }

    /**
     * 同步发送HTML邮件方法（支持多账号轮询）
     * @param from 邮件发送者，如果为null则使用轮询策略选择发送账号
     * @param to 收件人
     * @param subject 邮件主题
     * @param htmlContent html格式的邮件内容
     */
    public void sendHtmlMailSync(String from, String to, String subject, String htmlContent) {
        doSendHtmlMail(from, to, subject, htmlContent);
    }

    /**
     * 同步发送HTML邮件方法（支持多账号轮询，返回实际使用的发送账号）
     * @param from 邮件发送者，如果为null则使用轮询策略选择发送账号
     * @param to 收件人
     * @param subject 邮件主题
     * @param htmlContent html格式的邮件内容
     * @return 实际使用的发送账号邮箱地址
     */
    public String sendHtmlMailWithAccountSync(String from, String to, String subject, String htmlContent) {
        return doSendHtmlMail(from, to, subject, htmlContent);
    }

    /**
     * 发送HTML邮件的核心处理方法（支持多账号轮询和失败重试）
     * <p>如果指定了发送者则直接使用该账号发送，否则通过轮询策略选择账号；</p>
     * <p>发送失败时自动尝试其他可用账号。</p>
     * @param from 邮件发送者，如果为null则使用轮询策略选择发送账号
     * @param to 收件人
     * @param subject 邮件主题
     * @param htmlContent html格式的邮件内容
     * @return 实际使用的发送账号邮箱地址
     * @throws BizException 当没有可用账号或所有账号均发送失败时抛出
     */
    private String doSendHtmlMail(String from, String to, String subject, String htmlContent) {
        LocalDate today = LocalDate.now();

        if (from != null) {
            JavaMailSender sender = multiMailSenderConfig.getMailSender(from);
            if (sender == null) {
                throw new BizException("指定的发送邮箱不可用: " + from);
            }
            doSendMailInternal(sender, from, to, subject, htmlContent, today);
            return from;
        }

        MailAccount selectedAccount = mailAccountManagerService.selectAccountByRoundRobin();
        String selectedEmail = selectedAccount.getEmailAddress();
        JavaMailSender sender = multiMailSenderConfig.getMailSender(selectedEmail);

        if (sender == null) {
            throw new BizException("选中的发送邮箱不可用: " + selectedEmail);
        }

        try {
            doSendMailInternal(sender, selectedEmail, to, subject, htmlContent, today);
            return selectedEmail;
        } catch (BizException e) {
            mailAccountManagerService.recordFailure(selectedEmail, today);

            List<MailAccount> availableAccounts = mailAccountManagerService.getAvailableAccounts();
            if (availableAccounts.isEmpty()) {
                throw new BizException("没有可用的邮箱账号");
            }
            for (MailAccount account : availableAccounts) {
                if (!account.getEmailAddress().equals(selectedEmail)) {
                    JavaMailSender retrySender = multiMailSenderConfig.getMailSender(account.getEmailAddress());
                    if (retrySender != null && !mailAccountManagerService.isAccountOverLimit(account, today)) {
                        try {
                            doSendMailInternal(retrySender, account.getEmailAddress(), to, subject, htmlContent, today);
                            return account.getEmailAddress();
                        } catch (Exception retryEx) {
                            mailAccountManagerService.recordFailure(account.getEmailAddress(), today);
                            log.warn("重试邮箱 {} 发送失败: {}", account.getEmailAddress(), retryEx.getMessage());
                        }
                    }
                }
            }
            log.warn("所有可用邮箱账号发送失败");
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
    private void doSendMailInternal(JavaMailSender sender, String from, String to, String subject, String htmlContent, LocalDate today) {
        try {
            MimeMessage message = sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            sender.send(message);
            
            // 记录发送成功统计
            mailAccountManagerService.recordSuccess(from, today);
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
