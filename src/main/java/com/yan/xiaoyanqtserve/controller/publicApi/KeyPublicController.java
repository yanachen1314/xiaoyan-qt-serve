package com.yan.xiaoyanqtserve.controller.publicApi;

import com.yan.xiaoyanqtserve.common.annotaions.RateLimiter;
import com.yan.xiaoyanqtserve.common.annotaions.VerifyCaptcha;
import com.yan.xiaoyanqtserve.common.dto.EmailApplyDto;
import com.yan.xiaoyanqtserve.common.enums.ApplyStatus;
import com.yan.xiaoyanqtserve.common.enums.SceneIdEnum;
import com.yan.xiaoyanqtserve.common.vo.EmailVerifyVo;
import com.yan.xiaoyanqtserve.common.dto.key.KeyActiveDto;
import com.yan.xiaoyanqtserve.common.enums.RateLimiterType;
import com.yan.xiaoyanqtserve.common.utils.*;
import com.yan.xiaoyanqtserve.common.vo.KeyActiveVo;
import com.yan.xiaoyanqtserve.config.custome.MailPropertiesConfig;
import com.yan.xiaoyanqtserve.config.custome.WebInfoPropertiesConfig;
import com.yan.xiaoyanqtserve.entity.KeyInfo;
import com.yan.xiaoyanqtserve.entity.MailApplyRecord;
import com.yan.xiaoyanqtserve.service.MailApplyRecordService;
import com.yan.xiaoyanqtserve.service.SysConfigService;
import com.yan.xiaoyanqtserve.service.impl.apikey.KeyInfoManagerService;
import com.yan.xiaoyanqtserve.service.impl.mail.MailApplyManagerService;
import com.yan.xiaoyanqtserve.service.impl.mail.MailPushService;
import com.yan.xiaoyanqtserve.service.impl.mail.TemplateService;
import com.yan.xiaoyanqtserve.validator.EmailValidator;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * 密钥公开控制器
 * @author yanchen
 * @since 2026/3/10 15:11
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class KeyPublicController {
    private final KeyInfoManagerService keyinfoManagerService;
    private final MailPushService mailPushService;
    private final SysConfigService sysConfigService;
    private final TemplateService templateService;

    private final MailApplyRecordService mailApplyRecordService;
    private final MailApplyManagerService mailApplyManagerService;

    private final MailPropertiesConfig mailPropertiesConfig;
    private final WebInfoPropertiesConfig webInfoPropertiesConfig;

    /**
     * 密钥激活接口
     * <p>
     * 一个IP地址每秒只能请求10次
     * @param keyActiveDto 激活密钥参数
     * @return 激活结果
     */
    @RateLimiter(key = "active", time = 1, count = 10, limitBy = RateLimiterType.IP)
    @PostMapping("/active")
    public ApiResponse<KeyActiveVo> activateKey(@Valid @RequestBody KeyActiveDto keyActiveDto,
                                                HttpServletRequest request) {
        String apiKeyFromHeader = NetUtil.extractApiKeyFromHeader(request);
        log.debug("Key activated: {} for device {}", apiKeyFromHeader, keyActiveDto.getDeviceID());
        if (apiKeyFromHeader == null) {
            return ApiResponse.fail("API密钥不能为空");
        }
        // 调用 KeyInfoService 的 activateKey 方法激活密钥
        KeyInfo keyInfo = keyinfoManagerService.activateKey(keyActiveDto, apiKeyFromHeader);

        // 激活成功
        Integer totalUses = keyInfo.getTotalUses();
        Integer remainingUses = keyInfo.getRemainingUses();
        Integer activationsNum = keyInfo.getActivationsNum();
        Integer remainingActiveNum = keyInfo.getRemainingActiveNum();

        String residualUsageView;
        if (totalUses > 0 && remainingUses > 0) {
            residualUsageView = remainingUses + "次";
        } else if (totalUses > 0 && remainingUses == 0) {
            residualUsageView = "已用完";
        } else {
            residualUsageView = "无限制";
        }

        String remainingActiveNumView;
        if (activationsNum > 0 && remainingActiveNum > 0) {
            remainingActiveNumView = remainingActiveNum + "次";
        } else if (activationsNum > 0 && remainingActiveNum == 0) {
            remainingActiveNumView = "已用完";
        } else {
            remainingActiveNumView = "无限制";
        }
        return ApiResponse.success("密钥激活成功", KeyActiveVo.builder()
                .apiKey(keyInfo.getKeyValue())
                .devices(keyInfo.getActivationDevice())
                .expireTime(keyInfo.getExpiryTime())
                .totalUsage(totalUses == 0 ? "无限制" : totalUses + "次")
                .residualUsage(residualUsageView)
                .activationNums(activationsNum == 0 ? "无限制" : activationsNum + "次")
                .remainingActiveNum(remainingActiveNumView)
                .build()
        );
    }

    /**
     * 申请邮箱验证接口
     * <p>
     * 用户提交邮箱，服务器发送验证链接到用户邮箱，需点击链接进行验证
     * </p>
     * <p>
     * 当前业务：免费密钥申请接口，邮箱轮询发送免费密钥，一个邮箱只能领取一次
     * </p>
     * @see KeyPublicController#verifyEmailLink(String) /verifyEmailLink（链接验证接口）
     * @param emailApplyDto 包含收件人邮箱
     * @return 提示信息
     */
    @PostMapping("/applyEmailVerifyLink")
    @RateLimiter(key = "applyEmailVerify", time = 30, count = 1, limitBy = RateLimiterType.DEVICE_ID)
    @VerifyCaptcha(sceneId = SceneIdEnum.EMAIL_APPLY)
    public ApiResponse<EmailVerifyVo> applyEmailVerifyLink(@Valid @RequestBody EmailApplyDto emailApplyDto,
                                                           HttpServletRequest request) {
        String applicantEmail = emailApplyDto.getEmail();
        String deviceType = NetUtil.getClientDeviceType(request);
        String deviceId = NetUtil.getClientDeviceId(request);
        String clientIp = NetUtil.getClientIpAddress(request);
        String userAgent = NetUtil.getUserAgent(request);
        log.debug("申请邮箱验证：{}，设备类型：{}，设备ID：{}，客户端IP：{}，用户代理：{}",
                applicantEmail, deviceType, deviceId, clientIp, userAgent);
        try {
            // 1. 邮箱校验
            List<String> emailSuffixList = sysConfigService.getEmailSuffixList();
            if (!EmailValidator.isValidEmail(applicantEmail, emailSuffixList)) {
                throw new IllegalArgumentException("邮箱格式不正确");
            }

            // 2. 检查邮箱是否已验证或已领取
            if (mailApplyRecordService.hasVerified(applicantEmail)) {
                throw new IllegalArgumentException("该邮箱已验证，请勿重复申请");
            }
            if (mailApplyRecordService.hasSentKey(applicantEmail)) {
                throw new IllegalArgumentException("卡密已发送，请注意查收邮件");
            }

            // 3. 检查邮箱是否已申请但未验证
            if (mailApplyRecordService.hasPendingVerification(applicantEmail)) {
                return ApiResponse.success("验证邮件已发送，请检查邮箱并在30分钟内完成验证",
                        EmailVerifyVo.builder()
                                .email(applicantEmail)
                                .message("验证邮件已发送，请检查邮箱并在30分钟内完成验证")
                                .build()
                );
            }

            // 4. 生成验证token和过期时间
            String verifyToken = mailApplyManagerService.generateVerifyToken();
            MailPropertiesConfig.VerifyLinkConfig verifyLink = mailPropertiesConfig.getVerifyLink();
            // 过期时间内激活并使用密钥，单位为天
            int expireTimeInDays = verifyLink.getExpireTime();
            LocalDateTime expireTime = LocalDateTime.now().plusDays(expireTimeInDays);

            // 5. 构建验证链接URL
            String verifyUrl = webInfoPropertiesConfig.getApiSite() + verifyLink.getBasePath() + "?token=" + verifyToken;

            // 6. 保存验证记录（status=0）
            MailApplyRecord record = MailApplyRecord.builder()
                    .applicantEmail(applicantEmail)
                    .verifyToken(verifyToken)
                    .applyStatus(ApplyStatus.NOT_VERIFIED.getCode())
                    .verifyExpireTime(expireTime)
                    .deviceType(deviceType)
                    .deviceId(deviceId)
                    .clientIp(clientIp)
                    .userAgent(userAgent)
                    .build();
            mailApplyManagerService.saveApplyRecordSync(record);

            // 7. 异步发送验证邮件
            mailPushService.sendEmailVerifyLink(applicantEmail, verifyUrl)
                    .thenAccept(systemSenderEmail -> {
                        log.debug("验证邮件发送成功，邮箱: {}, 发送账号: {}", applicantEmail, systemSenderEmail);
                    })
                    .exceptionally(ex -> {
                        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                        log.error("验证邮件发送失败，邮箱: {}, 原因: {}", applicantEmail, cause.getMessage(), cause);
                        return null;
                    });

            // 8. 立即返回成功提示
            return ApiResponse.success("验证邮件已发送，请检查邮箱并在" + expireTimeInDays + "天内完成验证",
                    EmailVerifyVo.builder()
                            .email(applicantEmail)
                            .message("验证邮件已发送，请检查邮箱并在" + expireTimeInDays + "天内完成验证")
                            .build()
            );
        } catch (Exception e) {
            log.error("邮箱验证申请失败: {}", e.getMessage(), e);
            return ApiResponse.fail(e.getMessage(),
                    EmailVerifyVo.builder()
                            .email(applicantEmail)
                            .build()
            );
        }
    }

    /**
     * 验证邮箱链接接口
     * <p>
     * 用户点击邮件中的验证链接，服务器验证token后直接在页面中展示卡密
     * </p>
     * @see MailPropertiesConfig#getVerifyLink() 验证链接配置
     * @param token 验证token
     * @return HTML页面显示验证结果
     */
    @RateLimiter(key = "verifyEmailLink", time = 60, count = 3, limitBy = RateLimiterType.IP)
    @GetMapping(value = "/verifyEmailLink", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> verifyEmailLink(@RequestParam("token") String token) {
        log.debug("邮箱验证请求，token: {}", token);
        try {
            // 1. 根据token查询验证记录
            MailApplyRecord record = mailApplyRecordService.getByToken(token);
            if (record == null) {
                return ResponseEntity.ok(renderVerifyEmailFailurePage("验证链接无效", "该验证链接不存在或已被使用，请返回邮件重新点击验证链接。"));
            }

            // 2. 验证是否已过期
            if (record.getVerifyExpireTime().isBefore(LocalDateTime.now())) {
                return ResponseEntity.ok(renderVerifyEmailFailurePage("验证链接已过期", "该验证链接已过期，请返回邮件重新点击验证链接。"));
            }

            // 3. 验证状态是否为已验证或已发送卡密
            if (Objects.equals(record.getApplyStatus(), ApplyStatus.HAS_SENT.getCode())) {
                return ResponseEntity.ok(renderVerifyEmailSuccessPage(record.getApplicantEmail(), record.getApplyApiKey()));
            }

            // 4. 验证状态是否为未验证
            if (!Objects.equals(record.getApplyStatus(), ApplyStatus.NOT_VERIFIED.getCode())) {
                return ResponseEntity.ok(renderVerifyEmailFailurePage("验证链接已使用", "该验证链接已被使用，请勿重复点击。"));
            }

            // 5. 从卡密池获取卡密
            String licenseKey;
            try {
                licenseKey = keyinfoManagerService.getUnusedFreeKeyFromPool();
            } catch (Exception keyEx) {
                log.error("获取卡密失败，邮箱: {}, 原因: {}", record.getApplicantEmail(), keyEx.getMessage(), keyEx);
                return ResponseEntity.ok(renderVerifyEmailFailurePage("获取卡密失败", keyEx.getMessage() ));
            }

            // 6. 更新记录状态和卡密字段
            mailApplyManagerService.updateApplyRecordOnSendSuccess(record.getId(), licenseKey);

            // 7. 返回验证成功HTML页面（密钥直接展示在页面中）
            return ResponseEntity.ok(renderVerifyEmailSuccessPage(record.getApplicantEmail(), licenseKey));
        } catch (Exception e) {
            log.error("邮箱验证失败: {}", e.getMessage(), e);
            return ResponseEntity.ok(renderVerifyEmailFailurePage("验证失败", "验证过程中发生错误，请稍后重试。"));
        }
    }

    /**
     * 渲染邮箱验证成功页面
     * @param email 验证成功的邮箱地址
     * @param licenseKey 申请到的卡密
     * @return HTML页面内容
     */
    private String renderVerifyEmailSuccessPage(String email, String licenseKey) {
        return templateService.generateVerifyEmailSuccessPage(email, licenseKey);
    }

    /**
     * 渲染邮箱验证失败页面
     * @param title 错误标题
     * @param message 错误详情
     * @return HTML页面内容
     */
    private String renderVerifyEmailFailurePage(String title, String message) {
        return templateService.generateVerifyEmailFailurePage(title, message);
    }

}
