package com.yan.xiaoyanqtserve.controller;

import com.yan.xiaoyanqtserve.service.mail.MailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @GetMapping("/sendMail")
    public void sendMail() {
        mailService.sendHtmlMail("2742015729@qq.com", "443421279@qq.com", "测试邮件", "测试邮件");
    }
}
