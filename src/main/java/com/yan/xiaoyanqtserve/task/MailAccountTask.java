package com.yan.xiaoyanqtserve.task;

import com.yan.xiaoyanqtserve.common.enums.EmailServiceProvider;
import com.yan.xiaoyanqtserve.config.MultiMailSenderConfig;
import com.yan.xiaoyanqtserve.entity.MailAccount;
import com.yan.xiaoyanqtserve.service.mail.MailAccountManagerService;
import com.yan.xiaoyanqtserve.service.mail.MailPushService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * 邮件系统账号定时任务
 * @author yanchen
 * @since 2025/11/29 15:49
 */
@Slf4j
@Component
public class MailAccountTask {

    @Resource
    private MailPushService mailPushService;

    @Resource
    private MailAccountManagerService mailAccountManagerService;

    @Resource
    private MultiMailSenderConfig multiMailSenderConfig;

    /**
     * 每天12点检测系统邮箱授权密码是否过期
     * @see EmailServiceProvider
     */
    @Scheduled(cron = "0 0 12 * * ?")
    public void checkAuthPasswordExpiry() {
        try {
            for (MailAccount account : mailAccountManagerService.getEnabledAccounts()) {
                String emailAddress = account.getEmailAddress();
                EmailServiceProvider provider = EmailServiceProvider.fromEmailAddress(emailAddress);

                log.debug("检查邮箱[{}]，服务商: {}", emailAddress, provider.getDisplayName());

                // 只处理有明确有效期的服务商
                if (provider.getPasswordValidityPeriod() != Integer.MAX_VALUE) {
                    LocalDate activeDate = account.getPasswordCreateTime().toLocalDate();
                    if (activeDate == null) {
                        log.warn("邮箱[{}]未设置授权密码激活日期", emailAddress);
                        continue;
                    }

                    LocalDate expiryDate = activeDate.plus(provider.getPasswordValidityPeriod(), provider.getValidityUnit());
                    LocalDate now = LocalDate.now();
                    long daysUntilExpiry = ChronoUnit.DAYS.between(now, expiryDate);

                    log.info("邮箱[{}]授权码: 激活日期={}, 过期日期={}, 剩余{}天",
                            emailAddress, activeDate, expiryDate, daysUntilExpiry);

                    // 使用服务商特定的提醒阈值
                    if (daysUntilExpiry <= provider.getExpiryWarningThreshold()) {
                        mailPushService.sendAuthExpiryWarningMail(
                                account, provider, activeDate, expiryDate, daysUntilExpiry);
                    }
                }
            }
        } catch (Exception e) {
            log.error("检查授权码过期时间时发生异常: {}", e.getMessage(), e);
        }
    }

    /**
     * 定时刷新邮件发送器配置，每10分钟检查一次数据库是否有新增/变更的账号
     */
    @Scheduled(fixedRate = 600000)
    public void scheduledReload() {
        log.debug("定时刷新邮件发送器配置...");
        multiMailSenderConfig.reloadMailSenders();
    }


}
