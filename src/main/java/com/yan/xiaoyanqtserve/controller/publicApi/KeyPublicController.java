package com.yan.xiaoyanqtserve.controller.publicApi;

import com.yan.xiaoyanqtserve.common.annotaions.RateLimiter;
import com.yan.xiaoyanqtserve.common.annotaions.VerifyCaptcha;
import com.yan.xiaoyanqtserve.common.dto.CaptchaVerifyVo;
import com.yan.xiaoyanqtserve.common.dto.FreeLicenseDto;
import com.yan.xiaoyanqtserve.common.dto.MailRecordDto;
import com.yan.xiaoyanqtserve.common.enums.SceneIdEnum;
import com.yan.xiaoyanqtserve.common.vo.FreeLicenseVo;
import com.yan.xiaoyanqtserve.common.dto.key.KeyActiveDto;
import com.yan.xiaoyanqtserve.common.enums.RateLimiterType;
import com.yan.xiaoyanqtserve.common.utils.*;
import com.yan.xiaoyanqtserve.common.vo.KeyActiveVo;
import com.yan.xiaoyanqtserve.entity.KeyInfo;
import com.yan.xiaoyanqtserve.service.MailFreeKeyRecordService;
import com.yan.xiaoyanqtserve.service.SysConfigService;
import com.yan.xiaoyanqtserve.service.impl.KeyInfoManagerService;
import com.yan.xiaoyanqtserve.service.mail.MailRecordSyncService;
import com.yan.xiaoyanqtserve.service.mail.MailService;
import com.yan.xiaoyanqtserve.validator.EmailValidator;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    private final KeyInfoManagerService managerService;
    private final MailService mailService;
    private final MailFreeKeyRecordService mailFreeKeyRecordService;
    private final SysConfigService sysConfigService;
    private final MailRecordSyncService mailRecordSyncService;

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
        KeyInfo keyInfo = managerService.activateKey(keyActiveDto, apiKeyFromHeader);

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
     * 免费密钥申请接口
     * <p>
     * 邮箱轮询发送免费密钥，一个邮箱只能领取一次
     * </p>
     * @param freeLicenseDto 包含收件人邮箱及自动填充的设备信息
     * @return 提示信息
     */
    @PostMapping("/freeLicense")
    @RateLimiter(key = "freeLicense", time = 30, count = 1, limitBy = RateLimiterType.DEVICE_ID)
    @VerifyCaptcha(sceneId = SceneIdEnum.EMAIL_APPLY)
    public ApiResponse<CaptchaVerifyVo> sendMailRoundRobin(@Valid @RequestBody FreeLicenseDto freeLicenseDto,
                                                           HttpServletRequest request) {
        String applicantEmail = freeLicenseDto.getEmail();
        String deviceType = NetUtil.getClientDeviceType(request);
        String deviceId = NetUtil.getClientDeviceId(request);
        String clientIp = NetUtil.getClientIpAddress(request);
        String userAgent = request.getHeader("User-Agent");
        log.debug("申请邮箱：{}，设备类型：{}，设备ID：{}，客户端IP：{}，用户代理：{}", applicantEmail, deviceType, deviceId, clientIp, userAgent);
        try {
            // 1. 邮箱校验
            // 邮箱账号格式校验
            List<String> emailSuffixList = sysConfigService.getEmailSuffixList();
            if (!EmailValidator.isValidEmail(applicantEmail, emailSuffixList)) {
                throw new IllegalArgumentException("邮箱格式不正确");
            }
            // 检查该邮箱是否已领取
            if (mailFreeKeyRecordService.hasReceived(applicantEmail)) {
                throw new IllegalArgumentException("该邮箱已领取过免费卡密，请勿重复领取");
            }

            // 2. 从卡密池中获取一个未使用的免费卡密
            String licenseKey = managerService.getUnusedFreeKeyFromPool();

            // 3. 异步发送邮件并保存记录
            mailService.sendFreeLicenseEmail(applicantEmail, licenseKey)
                    .thenAccept(systemSenderEmail -> {
                        // 发送成功，异步记录领取信息
                        mailRecordSyncService.saveRecordAsync(MailRecordDto.builder()
                                .systemSenderEmail(systemSenderEmail)
                                .resultStatus(1)
                                .resultMsg("发送成功")
                                .applicantEmail(applicantEmail)
                                .licenseKey(licenseKey)
                                .deviceType(deviceType)
                                .deviceId(deviceId)
                                .clientIp(clientIp)
                                .userAgent(userAgent)
                                .build());
                    })
                    .exceptionally(ex -> {
                        // 发送失败，记录失败信息
                        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                        log.error("邮件发送失败，邮箱: {}, 原因: {}", applicantEmail, cause.getMessage(), cause);
                        mailRecordSyncService.saveRecordAsync(MailRecordDto.builder()
                                .systemSenderEmail("")
                                .resultStatus(0)
                                .resultMsg(cause.getMessage())
                                .applicantEmail(applicantEmail)
                                .licenseKey(licenseKey)
                                .deviceType(deviceType)
                                .deviceId(deviceId)
                                .clientIp(clientIp)
                                .userAgent(userAgent)
                                .build());
                        return null;
                    });

            // 4. 立即返回结果，不等待邮件发送完成
            return ApiResponse.success("免费卡密发送成功，请注意查收邮件", CaptchaVerifyVo
                    .buildSuccess(FreeLicenseVo.builder().email(applicantEmail).build())
            );
        } catch (Exception e) {
            log.error("免费卡密领取失败: {}", e.getMessage(), e);
            // 如果是重复领取（并发情况下的唯一索引冲突）
            if (e.getMessage() != null && e.getMessage().contains("Duplicate")) {
                // return ApiResponse.fail("该邮箱已领取过免费卡密，请勿重复领取");
                return ApiResponse.fail("该邮箱已领取过免费卡密，请勿重复领取",
                        CaptchaVerifyVo.buildFail(FreeLicenseVo.builder().build())
                );

            }
            return ApiResponse.fail(e.getMessage(),
                    CaptchaVerifyVo.buildFail(FreeLicenseVo.builder().build())
            );
        }
    }

}
