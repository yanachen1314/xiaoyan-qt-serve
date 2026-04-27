package com.yan.xiaoyanqtserve.service.captcha;

import com.aliyun.captcha20230305.Client;
import com.aliyun.captcha20230305.models.VerifyIntelligentCaptchaRequest;
import com.aliyun.captcha20230305.models.VerifyIntelligentCaptchaResponse;
import com.aliyun.captcha20230305.models.VerifyIntelligentCaptchaResponseBody;
import com.aliyun.tea.TeaException;
import com.aliyun.teaopenapi.models.Config;
import com.yan.xiaoyanqtserve.service.SysConfigService;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;

/**
 * 阿里云验证码2.0封装服务（V2版）- 配置驱动实现
 * @author yanchen
 * @since 2026/4/27
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AliyunCaptchaService {

    private final CaptchaProperties properties;
    private final SysConfigService sysConfigService;

    // 阿里云Client
    private Client client;


    /**
     * 初始化阿里云Client（单例复用）
     */
    @PostConstruct
    public void init() {
        if (!sysConfigService.getCaptchaServiceEnabled()) {
            log.warn("阿里云验证码服务已禁用，跳过初始化");
            return;
        }

        try {
            Config config = new Config()
                    .setAccessKeyId(properties.getAccessKeyId())
                    .setAccessKeySecret(properties.getAccessKeySecret())
                    .setEndpoint(properties.getEndpoint())
                    .setConnectTimeout(properties.getConnectTimeout())
                    .setReadTimeout(properties.getReadTimeout());

            this.client = new Client(config);
            log.info("阿里云验证码Client初始化成功 | Endpoint: {} | Timeout: {}/{}ms",
                    properties.getEndpoint(),
                    properties.getConnectTimeout(),
                    properties.getReadTimeout());
        } catch (Exception e) {
            log.error("阿里云验证码Client初始化失败，请检查配置", e);
            throw new IllegalStateException("阿里云验证码服务初始化失败", e);
        }
    }

    /**
     * 验证验证码
     * @param sceneId 验证场景ID,服务端写入本次验证对应的场景ID，建议传入，尤其在多场景下，防止前端被篡改为其他场景。
     * @param captchaVerifyParam 前端传来的验证参数
     * @return 验证结果（true=通过，false=不通过）
     */
    public boolean verifyCaptcha(String sceneId, String captchaVerifyParam) {
        // 服务禁用时直接通过验证
        if (!sysConfigService.getCaptchaServiceEnabled()) {
            log.debug("验证码服务已禁用，自动通过验证");
            return true;
        }

        if (client == null) {
            log.error("阿里云Client未初始化，请检查配置");
            return handleServiceException(new IllegalStateException("Client未初始化"));
        }

        VerifyIntelligentCaptchaRequest request = new VerifyIntelligentCaptchaRequest()
                .setSceneId(sceneId)
                .setCaptchaVerifyParam(captchaVerifyParam);

        try {
            VerifyIntelligentCaptchaResponse response = client.verifyIntelligentCaptcha(request);
            // HTTP状态码
            Integer httpStatusCode = response.getStatusCode();
            VerifyIntelligentCaptchaResponseBody responseBody = response.getBody();
            // 请求是否成功.true：表示请求成功, false：表示请求失败。
            Boolean success = responseBody.getSuccess();
            // 返回码,如：Success、InternalError
            String code = responseBody.getCode();
            // 返回码描述
            String message = responseBody.getMessage();
            // 验证结果。true：表示验证通过, false：表示验证不通过。
            Boolean verifyResult = responseBody.getResult().getVerifyResult();
            // 验证结果代码，如：T001，F001，F002
            String verifyCode = responseBody.getResult().getVerifyCode();

            log.debug("阿里云验证码服务调用结果（Scene: {}） " +
                            "| HTTPStatusCode: {} | Success: {} | Code: {} | Message: {} | VerifyResult: {} | VerifyCode: {}",
                    sceneId, httpStatusCode, success, code, message, verifyResult, verifyCode);

            return Boolean.TRUE.equals(verifyResult);
        } catch (TeaException e) {
            log.error("阿里云验证码服务调用异常 [HTTP:{}] [Code:{}] [Message:{}] [Recommend:{}]",
                    e.getCode(),
                    e.getData().get("Code"),
                    e.getMessage(),
                    e.getData().get("Recommend"),
                    e);
            return handleServiceException(e);
        } catch (Exception e) {
            log.error("验证码验证发生未预期异常", e);
            return handleServiceException(e);
        }
    }

    /**
     * 异常处理策略：优先保证业务可用
     */
    private boolean handleServiceException(Exception e) {
        // 生产环境建议添加熔断机制
        log.warn("验证码服务异常降级处理 - 业务优先策略启用 (异常: {})", e.getClass().getSimpleName());
        return true;
    }


    /**
     * 阿里云验证码配置属性
     */
    @Data
    @Configuration
    @ConfigurationProperties(prefix = "aliyun.captcha")
    public static class CaptchaProperties {
        /**
         * AccessKey ID（必须）
         */
        private String accessKeyId;

        /**
         * AccessKey Secret（必须）
         */
        private String accessKeySecret;

        /**
         * 服务端点（默认：captcha.cn-shanghai.aliyuncs.com）
         */
        private String endpoint = "captcha.cn-shanghai.aliyuncs.com";

        /**
         * 连接超时（毫秒，默认：5000）
         */
        private Integer connectTimeout = 5000;

        /**
         * 读取超时（毫秒，默认：5000）
         */
        private Integer readTimeout = 5000;

    }
}