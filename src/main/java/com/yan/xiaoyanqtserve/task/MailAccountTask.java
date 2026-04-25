package com.yan.xiaoyanqtserve.task;

import com.yan.xiaoyanqtserve.config.MultiMailSenderConfig;
import com.yan.xiaoyanqtserve.service.mail.MailService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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

    // 授权码激活时间，格式：yyyy-MM-dd
    @Value("${spring.mail.pwd-active-time}")
    private String pwdActiveTime;

    @Resource
    private MailService mailService;

    @Resource
    private MultiMailSenderConfig multiMailSenderConfig;

    /**
     * 每天12点检测网易邮箱授权密码是否过期（180天内有效）
     */
    @Scheduled(cron = "0 0 12 * * ?")
    public void MailPwdReset() {
        try {
            // 授权密码启用时间
            LocalDate activeDate = LocalDate.parse(pwdActiveTime);
            // 过期时间（180天后）
            LocalDate expiryDate = activeDate.plusDays(180);
            // 当前时间
            LocalDate now = LocalDate.now();
            // 剩余有效天数
            long daysUntilExpiry = ChronoUnit.DAYS.between(now, expiryDate);

            // 如果剩余有效天数小于等于7天，则发送邮件提醒
            if(daysUntilExpiry <= 7) {
                mailService.sendNeteaseAuthCodeMail(activeDate, expiryDate, now, daysUntilExpiry);
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
