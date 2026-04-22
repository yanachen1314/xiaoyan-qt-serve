package com.yan.xiaoyanqtserve.controller;

import com.yan.xiaoyanqtserve.config.MultiMailSenderConfig;
import com.yan.xiaoyanqtserve.entity.MailAccount;
import com.yan.xiaoyanqtserve.service.mail.MailAccountService;
import com.yan.xiaoyanqtserve.service.mail.MailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@RequiredArgsConstructor
public class TestController {
    private final MailService mailService;
    private final MailAccountService mailAccountService;
    private final MultiMailSenderConfig multiMailSenderConfig;

    @GetMapping("/sendMail")
    public String sendMail() {
        mailService.sendHtmlMail("2742015729@qq.com", "443421279@qq.com", "测试邮件", "测试邮件内容");
        return "邮件发送请求已提交";
    }

    @GetMapping("/sendMailRoundRobin")
    public String sendMailRoundRobin(@RequestParam(defaultValue = "443421279@qq.com") String to) {
        mailService.sendHtmlMail(null, to, "测试邮件-轮询发送", "这是一封通过轮询策略发送的测试邮件");
        return "邮件发送请求已提交（轮询模式）";
    }

    @GetMapping("/sendVerifyCode")
    public String sendVerifyCode(@RequestParam(defaultValue = "443421279@qq.com") String to) {
        mailService.sendVerifyCodeMail(to, "密码重置验证", "123456", 5);
        return "验证码邮件发送请求已提交";
    }

    @GetMapping("/mailAccounts")
    public Map<String, Object> getMailAccounts() {
        List<MailAccount> accounts = mailAccountService.getEnabledAccounts();
        List<MailAccount> availableAccounts = mailAccountService.getAvailableAccounts();
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
            
            long sendCount = mailAccountService.getSendCount(account.getEmailAddress(), today);
            long successCount = mailAccountService.getSuccessCount(account.getEmailAddress(), today);
            long failCount = mailAccountService.getFailCount(account.getEmailAddress(), today);
            
            stats.put("todaySendCount", sendCount);
            stats.put("todaySuccessCount", successCount);
            stats.put("todayFailCount", failCount);
            stats.put("isOverLimit", mailAccountService.isAccountOverLimit(account, today));
            
            accountStats.put(account.getEmailAddress(), stats);
        }
        result.put("accounts", accountStats);
        
        return result;
    }
}
