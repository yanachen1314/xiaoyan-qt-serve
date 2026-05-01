package com.yan.xiaoyanqtserve.controller.publicApi;

import com.yan.xiaoyanqtserve.common.annotaions.RateLimiter;
import com.yan.xiaoyanqtserve.common.annotaions.VerifyCaptcha;
import com.yan.xiaoyanqtserve.common.dto.CaptchaVerifyVo;
import com.yan.xiaoyanqtserve.common.dto.EmailApplyDto;
import com.yan.xiaoyanqtserve.common.dto.FreeLicenseDto;
import com.yan.xiaoyanqtserve.common.dto.MailRecordDto;
import com.yan.xiaoyanqtserve.common.enums.ApplyStatus;
import com.yan.xiaoyanqtserve.common.enums.SceneIdEnum;
import com.yan.xiaoyanqtserve.common.vo.EmailVerifyVo;
import com.yan.xiaoyanqtserve.common.vo.FreeLicenseVo;
import com.yan.xiaoyanqtserve.common.dto.key.KeyActiveDto;
import com.yan.xiaoyanqtserve.common.enums.RateLimiterType;
import com.yan.xiaoyanqtserve.common.utils.*;
import com.yan.xiaoyanqtserve.common.vo.KeyActiveVo;
import com.yan.xiaoyanqtserve.common.Constants;
import com.yan.xiaoyanqtserve.entity.KeyInfo;
import com.yan.xiaoyanqtserve.entity.MailApplyRecord;
import com.yan.xiaoyanqtserve.service.MailApplyRecordService;
import com.yan.xiaoyanqtserve.service.SysConfigService;
import com.yan.xiaoyanqtserve.service.impl.apikey.KeyInfoManagerService;
import com.yan.xiaoyanqtserve.service.impl.mail.MailApplyManagerService;
import com.yan.xiaoyanqtserve.service.impl.mail.MailPushService;
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
import java.time.format.DateTimeFormatter;
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
    private final KeyInfoManagerService keyinfoManagerService;
    private final MailPushService mailPushService;
    private final MailApplyRecordService mailApplyRecordService;
    private final SysConfigService sysConfigService;
    private final MailApplyManagerService mailApplyManagerService;

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
     * 免费密钥申请接口
     * <p>
     * 邮箱轮询发送免费密钥，一个邮箱只能领取一次
     * </p>
     * @param freeLicenseDto 包含收件人邮箱及自动填充的设备信息
     * @return 提示信息
     */
    @PostMapping("/freeLicense")
    @RateLimiter(key = "freeLicense", time = 60, count = 1, limitBy = RateLimiterType.DEVICE_ID)
    @VerifyCaptcha(sceneId = SceneIdEnum.EMAIL_APPLY)
    public ApiResponse<CaptchaVerifyVo> sendMailRoundRobin(@Valid @RequestBody FreeLicenseDto freeLicenseDto,
                                                           HttpServletRequest request) {
        String applicantEmail = freeLicenseDto.getEmail();
        String deviceType = NetUtil.getClientDeviceType(request);
        String deviceId = NetUtil.getClientDeviceId(request);
        String clientIp = NetUtil.getClientIpAddress(request);
        String userAgent = NetUtil.getUserAgent(request);
        log.debug("申请邮箱：{}，设备类型：{}，设备ID：{}，客户端IP：{}，用户代理：{}",
                applicantEmail, deviceType, deviceId, clientIp, userAgent);
        try {
            // 1. 邮箱校验
            // 邮箱账号格式校验
            List<String> emailSuffixList = sysConfigService.getEmailSuffixList();
            if (!EmailValidator.isValidEmail(applicantEmail, emailSuffixList)) {
                throw new IllegalArgumentException("邮箱格式不正确");
            }
            // 检查该邮箱是否已领取
            if (mailApplyRecordService.hasReceived(applicantEmail)) {
                throw new IllegalArgumentException("该邮箱已领取过免费卡密，请勿重复领取");
            }

            // 2. 从卡密池中获取一个未使用的免费卡密
            String licenseKey = keyinfoManagerService.getUnusedFreeKeyFromPool();

            // 3. 异步发送邮件并保存记录
            mailPushService.sendFreeLicenseEmail(applicantEmail, licenseKey)
                    .thenAccept(systemSenderEmail -> {
                        // 发送成功，异步记录领取信息
                        mailApplyManagerService.saveRecordAsync(MailRecordDto.builder()
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
                        mailApplyManagerService.saveRecordAsync(MailRecordDto.builder()
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

    /**
     * 申请邮箱验证接口
     * <p>
     * 用户提交邮箱，服务器发送验证链接到邮箱
     * 一个邮箱只能申请一次验证
     * </p>
     * @param emailApplyDto 包含收件人邮箱
     * @return 提示信息
     */
    @PostMapping("/applyEmailVerify")
    @RateLimiter(key = "applyEmailVerify", time = 30, count = 1, limitBy = RateLimiterType.DEVICE_ID)
    @VerifyCaptcha(sceneId = SceneIdEnum.EMAIL_APPLY)
    public ApiResponse<EmailVerifyVo> applyEmailVerify(@Valid @RequestBody EmailApplyDto emailApplyDto,
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
            LocalDateTime expireTime = LocalDateTime.now().plusMinutes(Constants.EMAIL_VERIFY_EXPIRE_MINUTES);
            
            // 5. 构建验证链接URL
            String verifyUrl = Constants.EMAIL_VERIFY_BASE_URL + "?token=" + verifyToken;
            
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
            Long applyRecordId = mailApplyManagerService.saveApplyRecord(record);

            // 7. 异步发送验证邮件 todo:记录发送日志
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
            return ApiResponse.success("验证邮件已发送，请检查邮箱并在30分钟内完成验证",
                    EmailVerifyVo.builder()
                            .email(applicantEmail)
                            .message("验证邮件已发送，请检查邮箱并在30分钟内完成验证")
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
     * 用户点击邮件中的验证链接，服务器验证token后发送卡密邮件
     * </p>
     * @param token 验证token
     * @return HTML页面显示验证结果
     */
    @GetMapping(value = "/verifyEmail", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> verifyEmail(@RequestParam("token") String token) {
        log.debug("邮箱验证请求，token: {}", token);
        try {
            // 1. 根据token查询验证记录
            MailApplyRecord record = mailApplyRecordService.getByToken(token);
            if (record == null) {
                return ResponseEntity.ok(buildErrorHtmlPage("验证链接无效", "该验证链接不存在或已被使用，请重新申请。"));
            }
            
            // 2. 验证是否已过期
            if (record.getVerifyExpireTime().isBefore(LocalDateTime.now())) {
                return ResponseEntity.ok(buildErrorHtmlPage("验证链接已过期", "该验证链接已过期，请重新申请。"));
            }
            
            // 3. 验证状态是否为0（未验证）
            if (record.getApplyStatus() != 0) {
                return ResponseEntity.ok(buildErrorHtmlPage("验证链接已使用", "该验证链接已被使用，请勿重复点击。"));
            }
            
            // 4. 更新状态为1（已验证）
            mailApplyRecordService.updateStatus(record.getId(), ApplyStatus.HAS_VERIFIED);
            
            // 5. 从卡密池获取卡密
            String licenseKey = keyinfoManagerService.getUnusedFreeKeyFromPool();
            
            // 6. 更新记录的卡密字段
            MailApplyRecord updateRecord = new MailApplyRecord();
            updateRecord.setId(record.getId());
            updateRecord.setApplyApiKey(licenseKey);
            updateRecord.setApplyStatus((byte) 2);
            mailApplyRecordService.updateById(updateRecord);
            
            // 7. 异步发送卡密邮件
            String applicantEmail = record.getApplicantEmail();
            mailPushService.sendFreeLicenseEmail(applicantEmail, licenseKey)
                    .thenAccept(systemSenderEmail -> {
                        log.debug("卡密邮件发送成功，邮箱: {}, 发送账号: {}", applicantEmail, systemSenderEmail);
                        // 异步记录发送信息
                        mailApplyManagerService.saveRecordAsync(MailRecordDto.builder()
                                .systemSenderEmail(systemSenderEmail)
                                .resultStatus(1)
                                .resultMsg("发送成功")
                                .applicantEmail(applicantEmail)
                                .licenseKey(licenseKey)
                                .deviceType(record.getDeviceType())
                                .deviceId(record.getDeviceId())
                                .clientIp(record.getClientIp())
                                .userAgent(record.getUserAgent())
                                .build());
                    })
                    .exceptionally(ex -> {
                        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                        log.error("卡密邮件发送失败，邮箱: {}, 原因: {}", applicantEmail, cause.getMessage(), cause);
                        mailApplyManagerService.saveRecordAsync(MailRecordDto.builder()
                                .systemSenderEmail("")
                                .resultStatus(0)
                                .resultMsg(cause.getMessage())
                                .applicantEmail(applicantEmail)
                                .licenseKey(licenseKey)
                                .deviceType(record.getDeviceType())
                                .deviceId(record.getDeviceId())
                                .clientIp(record.getClientIp())
                                .userAgent(record.getUserAgent())
                                .build());
                        return null;
                    });
            
            // 8. 返回验证成功HTML页面
            return ResponseEntity.ok(buildSuccessHtmlPage(applicantEmail));
        } catch (Exception e) {
            log.error("邮箱验证失败: {}", e.getMessage(), e);
            return ResponseEntity.ok(buildErrorHtmlPage("验证失败", "验证过程中发生错误，请稍后重试。"));
        }
    }
    
    /**
     * 构建验证成功HTML页面
     * @param email 收件人邮箱
     * @return HTML字符串
     */
    private String buildSuccessHtmlPage(String email) {
        String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return """
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>验证成功 - 小烟改写工具</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body { font-family: Arial, 'Microsoft YaHei', sans-serif; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); min-height: 100vh; display: flex; align-items: center; justify-content: center; padding: 20px; }
        .container { background: white; border-radius: 16px; box-shadow: 0 10px 40px rgba(0,0,0,0.2); padding: 50px; max-width: 500px; width: 100%; text-align: center; }
        .icon { font-size: 80px; color: #4CAF50; margin-bottom: 20px; }
        h1 { color: #333; font-size: 28px; margin-bottom: 15px; }
        p { color: #666; font-size: 16px; line-height: 1.6; margin-bottom: 15px; }
        .email { background: #f5f5f5; padding: 15px; border-radius: 8px; font-weight: bold; color: #333; margin: 20px 0; }
        .tip { background: #e3f2fd; padding: 15px; border-radius: 8px; color: #1976d2; margin-top: 20px; font-size: 14px; }
        .footer { margin-top: 30px; color: #999; font-size: 12px; }
    </style>
</head>
<body>
    <div class="container">
        <div class="icon">✓</div>
        <h1>验证成功！</h1>
        <p>恭喜您，邮箱验证已成功完成。</p>
        <div class="email">卡密已发送到：%s</div>
        <p>请检查您的邮箱收件箱，卡密已自动发送。</p>
        <div class="tip">
            <strong>提示：</strong>如果未收到邮件，请检查垃圾邮件文件夹，或稍后重新申请。
        </div>
        <div class="footer">
            <p>验证时间：%s</p>
            <p>© 2025 小烟改写工具. 保留所有权利.</p>
        </div>
    </div>
</body>
</html>
""".formatted(email, currentTime);
    }

    /**
     * 构建验证失败HTML页面
     * @param title 错误标题
     * @param message 错误信息
     * @return HTML字符串
     */
    private String buildErrorHtmlPage(String title, String message) {
        String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return """
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>验证失败 - 小烟改写工具</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body { font-family: Arial, 'Microsoft YaHei', sans-serif; background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%); min-height: 100vh; display: flex; align-items: center; justify-content: center; padding: 20px; }
        .container { background: white; border-radius: 16px; box-shadow: 0 10px 40px rgba(0,0,0,0.2); padding: 50px; max-width: 500px; width: 100%; text-align: center; }
        .icon { font-size: 80px; color: #f44336; margin-bottom: 20px; }
        h1 { color: #333; font-size: 28px; margin-bottom: 15px; }
        p { color: #666; font-size: 16px; line-height: 1.6; margin-bottom: 15px; }
        .error-box { background: #ffebee; padding: 20px; border-radius: 8px; color: #c62828; margin: 20px 0; }
        .button { display: inline-block; background: #409EFF; color: white; padding: 12px 30px; border-radius: 25px; text-decoration: none; margin-top: 20px; font-weight: bold; }
        .button:hover { background: #3a8ee6; }
        .footer { margin-top: 30px; color: #999; font-size: 12px; }
    </style>
</head>
<body>
    <div class="container">
        <div class="icon">✗</div>
        <h1>%s</h1>
        <div class="error-box">%s</div>
        <p>如有问题，请访问我们的官方网站重新申请。</p>
        <a href="%s" class="button">前往官网</a>
        <div class="footer">
            <p>错误时间：%s</p>
            <p>© 2025 小烟改写工具. 保留所有权利.</p>
        </div>
    </div>
</body>
</html>
""".formatted(title, message, Constants.WEB_DOMAIN, currentTime);
    }

}
