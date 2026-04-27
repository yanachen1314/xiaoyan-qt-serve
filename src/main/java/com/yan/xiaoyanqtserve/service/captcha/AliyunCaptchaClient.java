package com.yan.xiaoyanqtserve.service.captcha;

import com.yan.xiaoyanqtserve.config.XiaoyanSecurityConfig;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

/**
 * 阿里云验证码2.0（V2版）发送客户端
 * @author yanchen
 * @since 2026/4/27 11:14
 */
@Component
public class AliyunCaptchaClient {
    @Resource
    private XiaoyanSecurityConfig xiaoyanSecurityConfig;

    private AliyunCaptchaClient(){

    }
}
