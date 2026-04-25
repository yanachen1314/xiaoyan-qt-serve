package com.yan.xiaoyanqtserve.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yan.xiaoyanqtserve.entity.MailAccount;
import com.yan.xiaoyanqtserve.mapper.MailAccountMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 多邮件发送器配置类
 * 应用启动时从数据库加载所有启用的邮箱账号，并为每个账号创建独立的JavaMailSender实例
 * 
 * @author yanchen
 * @since 2026/4/22
 */
@Slf4j
@Component
public class MultiMailSenderConfig implements ApplicationRunner {

    @Resource
    private MailAccountMapper mailAccountMapper;

    private final Map<String, JavaMailSender> mailSenderMap = new ConcurrentHashMap<>();

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("开始初始化邮件发送器...");
        reloadMailSenders();
        log.info("✅ 邮件发送器初始化完成，共加载 {} 个邮箱账号", mailSenderMap.size());
    }


    /**
     * 从数据库加载所有启用的邮箱账号并创建JavaMailSender
     */
    public void reloadMailSenders() {
        try {
            LambdaQueryWrapper<MailAccount> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(MailAccount::getStatus, 1);
            queryWrapper.orderByAsc(MailAccount::getPriority);
            List<MailAccount> accounts = mailAccountMapper.selectList(queryWrapper);

            Map<String, JavaMailSender> newMap = new ConcurrentHashMap<>();
            for (MailAccount account : accounts) {
                try {
                    JavaMailSender sender = createMailSender(account);
                    newMap.put(account.getEmailAddress(), sender);
                } catch (Exception e) {
                    log.error("创建邮件发送器失败: {}", account.getEmailAddress(), e);
                }
            }

            mailSenderMap.clear();
            mailSenderMap.putAll(newMap);
        } catch (Exception e) {
            log.error("刷新邮件发送器配置失败", e);
        }
    }

    /**
     * 根据邮箱账号配置创建JavaMailSender实例
     * 
     * @param account 邮箱账号配置
     * @return JavaMailSender实例
     */
    private JavaMailSender createMailSender(MailAccount account) {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(account.getHost());
        sender.setPort(account.getPort());
        sender.setUsername(account.getEmailAddress());
        sender.setPassword(account.getPassword());
        sender.setProtocol(account.getProtocol() != null ? account.getProtocol() : "smtp");

        Properties props = new Properties();
        props.put("mail.smtp.auth", true);
        props.put("mail.smtp.timeout", 5000);
        props.put("mail.smtp.connectiontimeout", 5000);

        if (account.getSslEnable() != null && account.getSslEnable() == 1) {
            props.put("mail.smtp.ssl.enable", true);
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.put("mail.smtp.socketFactory.fallback", false);
        }

        sender.setJavaMailProperties(props);
        return sender;
    }

    /**
     * 获取指定邮箱的JavaMailSender
     * 
     * @param emailAddress 邮箱地址
     * @return JavaMailSender实例，如果不存在则返回null
     */
    public JavaMailSender getMailSender(String emailAddress) {
        return mailSenderMap.get(emailAddress);
    }

    /**
     * 获取所有可用的邮箱地址列表
     * 
     * @return 邮箱地址列表
     */
    public List<String> getAllEmailAddresses() {
        return mailSenderMap.keySet().stream().toList();
    }

    /**
     * 获取邮件发送器Map（只读）
     * 
     * @return 邮件发送器Map
     */
    public Map<String, JavaMailSender> getMailSenderMap() {
        return Map.copyOf(mailSenderMap);
    }
}