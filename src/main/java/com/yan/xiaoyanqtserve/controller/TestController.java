package com.yan.xiaoyanqtserve.controller;

import com.yan.xiaoyanqtserve.common.utils.ApiResponse;
import com.yan.xiaoyanqtserve.config.MultiMailSenderConfig;
import com.yan.xiaoyanqtserve.config.XiaoyanSecurityConfig;
import com.yan.xiaoyanqtserve.entity.MailAccount;
import com.yan.xiaoyanqtserve.service.SysConfigService;
import com.yan.xiaoyanqtserve.service.impl.mail.MailAccountManagerService;
import com.yan.xiaoyanqtserve.service.impl.mail.MailPushService;
import com.yan.xiaoyanqtserve.task.MailRecordSyncTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 测试控制器
 * @author yanchen
 * @since 2026/4/22 16:26
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/api/public/test")
@Profile({"dev", "test"})
@RequiredArgsConstructor
public class TestController {
    private final MailPushService mailPushService;
    private final MailAccountManagerService mailAccountManagerService;
    private final MultiMailSenderConfig multiMailSenderConfig;
    private final MailRecordSyncTask mailRecordSyncTask;
    private final SysConfigService sysConfigService;
    private final XiaoyanSecurityConfig config;

    @GetMapping("/config/test")
    public ApiResponse<Map<String, Object>> testConfig() {

        return ApiResponse.success("获取配置成功", Map.of(
                "license", config.getLicense(),
                "bizData", config.getBizData(),
                "captchaServiceEnabled",sysConfigService.getCaptchaServiceEnabled()
        ));
    }

    @GetMapping("/sendMail")
    public String sendMail() {
        mailPushService.sendHtmlMail("2742015729@qq.com", "443421279@qq.com", "测试邮件", "测试邮件内容");
        return "邮件发送请求已提交";
    }

    @GetMapping("/sendMailRoundRobin")
    public String sendMailRoundRobin(@RequestParam(defaultValue = "443421279@qq.com") String to) {
        mailPushService.sendHtmlMail(null, to, "测试邮件-轮询发送", "这是一封通过轮询策略发送的测试邮件");
        return "邮件发送请求已提交（轮询模式）";
    }

    @GetMapping("/sendVerifyCode")
    public String sendVerifyCode(@RequestParam(defaultValue = "443421279@qq.com") String to) {
        mailPushService.sendVerifyCodeMail(to, "密码重置验证", "123456", 5);
        return "验证码邮件发送请求已提交";
    }

    @GetMapping("/mailAccounts")
    public Map<String, Object> getMailAccounts() {
        List<MailAccount> accounts = mailAccountManagerService.getEnabledAccounts();
        List<MailAccount> availableAccounts = mailAccountManagerService.getAvailableAccounts();
        LocalDate today = LocalDate.now();
        
        Map<String, Object> result = new HashMap<>();
        result.put("enabledCount", accounts.size());
        result.put("availableCount", availableAccounts.size());
        
        // 获取每个账号今天的发送统计
        Map<String, Object> accountStats = new HashMap<>();
        for (MailAccount account : accounts) {
            Map<String, Object> stats = new HashMap<>();
            stats.put("email", account.getEmailAddress());
            stats.put("dailyLimit", account.getDailyLimit());
            stats.put("status", account.getStatus());
            stats.put("priority", account.getPriority());
            
            long sendCount = mailAccountManagerService.getSendCount(account.getEmailAddress(), today);
            long successCount = mailAccountManagerService.getSendSuccessCount(account.getEmailAddress(), today);
            long failCount = mailAccountManagerService.getSendFailCount(account.getEmailAddress(), today);
            
            stats.put("todaySendCount", sendCount);
            stats.put("todaySuccessCount", successCount);
            stats.put("todayFailCount", failCount);
            stats.put("isOverLimit", mailAccountManagerService.isAccountOverLimit(account, today));
            
            accountStats.put(account.getEmailAddress(), stats);
        }
        result.put("accounts", accountStats);
        
        return result;
    }

    /**
     * 测试接口：手动触发邮件发送统计同步任务
     * @return 任务执行结果
     */
    @GetMapping("/syncMailRecords")
    public Map<String, Object> syncMailRecords() {
        Map<String, Object> result = new HashMap<>();
        try {
            log.info("手动触发邮件发送统计同步任务");
            mailRecordSyncTask.syncYesterdayRecords();
            result.put("success", true);
            result.put("message", "邮件发送统计同步任务执行完成");
        } catch (Exception e) {
            log.error("邮件发送统计同步任务执行失败", e);
            result.put("success", false);
            result.put("message", "任务执行失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 测试接口：手动触发免费密钥池重置和补充任务
     * @return 任务执行结果
     */
    @GetMapping("/resetFreeKeyPool")
    public Map<String, Object> resetFreeKeyPool() {
        Map<String, Object> result = new HashMap<>();
        try {
            log.info("手动触发免费密钥池重置和补充任务");
            mailRecordSyncTask.resetDailyFreeKeyCountAndReplenishPool();
            result.put("success", true);
            result.put("message", "免费密钥池重置和补充任务执行完成");
        } catch (Exception e) {
            log.error("免费密钥池重置和补充任务执行失败", e);
            result.put("success", false);
            result.put("message", "任务执行失败: " + e.getMessage());
        }
        return result;
    }
}
