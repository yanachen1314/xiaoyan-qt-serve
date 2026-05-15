package com.yan.xiaoyanqtserve.controller.publicApi;

import com.yan.xiaoyanqtserve.common.Constants;
import com.yan.xiaoyanqtserve.common.annotaions.RateLimiter;
import com.yan.xiaoyanqtserve.common.annotaions.VerifyCaptcha;
import com.yan.xiaoyanqtserve.common.dto.EmailApplyDto;
import com.yan.xiaoyanqtserve.common.enums.EmailApplyStatus;
import com.yan.xiaoyanqtserve.common.enums.SceneIdEnum;
import com.yan.xiaoyanqtserve.common.exception.BizException;
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
import com.yan.xiaoyanqtserve.service.impl.mail.MailAccountManagerService;
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
import org.springframework.util.StringUtils;
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
    private final MailAccountManagerService mailAccountManagerService;

    private final SysConfigService sysConfigService;
    private final TemplateService templateService;

    private final MailApplyRecordService mailApplyRecordService;
    private final MailApplyManagerService mailApplyManagerService;

    private final MailPropertiesConfig mailPropertiesConfig;
    private final WebInfoPropertiesConfig webInfoPropertiesConfig;
    private final RedisLockUtil redisLockUtil;

    /**
     * 密钥激活接口
     * <p>
     * 一个IP地址每秒只能请求10次
     * @param keyActiveDto 激活密钥参数
     * @return 激活结果
     */
    @RateLimiter(key = "active", time = 1, count = 10, limitBy = RateLimiterType.IP)
    @PostMapping("/active")
    public ApiResponse<KeyActiveVo> activateKey(@Valid @RequestBody KeyActiveDto keyActiveDto, HttpServletRequest request) {
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
        return ApiResponse.success("密钥激活成功", KeyActiveVo.builder().apiKey(keyInfo.getKeyValue()).devices(keyInfo.getActivationDevice()).expireTime(keyInfo.getExpiryTime()).totalUsage(totalUses == 0 ? "无限制" : totalUses + "次").residualUsage(residualUsageView).activationNums(activationsNum == 0 ? "无限制" : activationsNum + "次").remainingActiveNum(remainingActiveNumView).build());
    }

    /**
     * 申请邮箱验证接口
     * <p>
     * 用户提交邮箱，服务器发送验证链接到用户邮箱，需点击链接进行验证
     * </p>
     * <p>
     * 当前业务：免费密钥申请接口，邮箱轮询发送免费密钥，一个邮箱只能领取一次
     * </p>
     * <br/>
     * /verifyEmailLink（链接验证接口）: {@link KeyPublicController#verifyEmailLink(String)}
     * @param emailApplyDto 包含收件人邮箱
     * @return 提示信息
     */
    @PostMapping("/applyEmailLink")
    @RateLimiter(key = "applyEmailVerify", time = 30, count = 1, limitBy = RateLimiterType.DEVICE_ID)
    @VerifyCaptcha(sceneId = SceneIdEnum.EMAIL_APPLY)
    public ApiResponse<EmailVerifyVo> applyEmailLink(@Valid @RequestBody EmailApplyDto emailApplyDto, HttpServletRequest request) {
        String applicantEmail = emailApplyDto.getEmail();
        String deviceType = NetUtil.getClientDeviceType(request);
        String deviceId = NetUtil.getClientDeviceId(request);
        String clientIp = NetUtil.getClientIpAddress(request);
        String userAgent = NetUtil.getUserAgent(request);
        log.debug("申请邮箱验证：{}，设备类型：{}，设备ID：{}，客户端IP：{}，用户代理：{}", applicantEmail, deviceType, deviceId, clientIp, userAgent);
        try {
            // 1. 邮箱格式校验
            List<String> emailSuffixList = sysConfigService.getEmailSuffixList();
            if (!EmailValidator.isValidEmail(applicantEmail, emailSuffixList)) {
                throw new IllegalArgumentException("邮箱格式不正确");
            }
            // 2. 判断今日是否已经发出指定数量的免费密钥
            if (mailAccountManagerService.isTodayFreeKeyLimitReached()) {
                throw new BizException("今日免费密钥已发完了，明天早点来哦！");
            }

            MailPropertiesConfig.VerifyLinkConfig verifyLink = mailPropertiesConfig.getVerifyLink();
            int expireTimeInDays = verifyLink.getExpireTime();

            // 3.1 已申请且已成功发送过验证邮件，直接返回
            if (mailApplyManagerService.hasEmailApplyAndSendSuccess(applicantEmail)) {
                return buildApplySuccessResponse(applicantEmail, expireTimeInDays, "验证邮件请勿重复发送");
            }

            // 3.2 已申请但未发送过验证邮件或发送失败，重新生成token并发送
            if (mailApplyManagerService.hasEmailApplyButFail(applicantEmail)) {
                String verifyToken = mailApplyManagerService.generateVerifyToken();
                String verifyUrl = buildVerifyUrl(verifyLink, verifyToken);
                asyncSendVerifyEmail(applicantEmail, verifyUrl);
                return buildApplySuccessResponse(applicantEmail, expireTimeInDays, "请查收验证邮件");
            }

            // 3.3 未申请，创建记录并发送验证邮件
            String verifyToken = mailApplyManagerService.generateVerifyToken();
            String verifyUrl = buildVerifyUrl(verifyLink, verifyToken);
            LocalDateTime expireTime = LocalDateTime.now().plusDays(expireTimeInDays);
            // 保存申请记录
            MailApplyRecord record = MailApplyRecord.builder()
                    .applicantEmail(applicantEmail)
                    .verifyToken(verifyToken)
                    .applyStatus(EmailApplyStatus.PENDING.getCode())
                    .clickCount(0)
                    .verifyExpireTime(expireTime)
                    .deviceType(deviceType)
                    .deviceId(deviceId)
                    .clientIp(clientIp)
                    .userAgent(userAgent).build();
            mailApplyRecordService.saveApplyRecord(record);
            // 异步发送验证邮件
            asyncSendVerifyEmail(applicantEmail, verifyUrl);
            return buildApplySuccessResponse(applicantEmail, expireTimeInDays, "验证邮件已发送");
        } catch (BizException | IllegalArgumentException e) {
            log.warn("邮箱验证申请失败: {}", e.getMessage());
            return ApiResponse.fail(e.getMessage(), EmailVerifyVo.builder().email(applicantEmail).build());
        } catch (Exception e) {
            log.error("邮箱验证申请异常，邮箱: {}", applicantEmail, e);
            return ApiResponse.fail("申请失败，请稍后重试", EmailVerifyVo.builder().email(applicantEmail).build());
        }
    }

    /**
     * 构建验证链接
     * @param verifyLink 验证链接配置
     * @param verifyToken 验证Token
     * @return 完整的验证链接
     */
    private String buildVerifyUrl(MailPropertiesConfig.VerifyLinkConfig verifyLink, String verifyToken) {
        return webInfoPropertiesConfig.getApiSite() + verifyLink.getBasePath() + "?token=" + verifyToken;
    }

    /**
     * 异步发送验证邮件
     * @param email 收件邮箱
     * @param verifyUrl 验证链接
     */
    private void asyncSendVerifyEmail(String email, String verifyUrl) {
        mailPushService.sendEmailVerifyLink(email, verifyUrl)
                .thenAccept(senderEmail -> log.debug("验证邮件发送成功，邮箱: {}, 发送账号: {}", email, senderEmail))
                .exceptionally(ex -> {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    log.error("验证邮件发送失败，邮箱: {}, 原因: {}", email, cause.getMessage(), cause);
                    return null;
                });
    }

    /**
     * 构建邮箱验证申请成功响应
     * @param email 邮箱地址
     * @param expireDays 验证码有效期（天）
     * @param preHint 额外的提示信息
     * @return ApiResponse响应对象
     */
    private ApiResponse<EmailVerifyVo> buildApplySuccessResponse(String email, int expireDays, String preHint) {
        String hint = StringUtils.hasText(preHint) ? preHint + "，" : "";
        String message = hint + "请检查邮箱并在" + expireDays + "天内完成验证";
        return ApiResponse.success(message, EmailVerifyVo.builder().email(email).message(message).build());
    }

    /**
     * 验证邮箱链接接口
     * <p>
     * 用户点击邮件中的验证链接，服务器验证token后直接在页面中展示卡密
     * </p>
     * 验证链接配置: {@link MailPropertiesConfig#getVerifyLink()}
     * @param token 验证token
     * @return HTML页面显示验证结果
     */
    @RateLimiter(key = "verifyEmailLink", time = 60, count = 10, limitBy = RateLimiterType.IP)
    @GetMapping(value = "/verifyEmailLink", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> verifyEmailLink(@RequestParam("token") String token) {
        log.debug("邮箱验证请求，token: {}", token);
        String lockKey = Constants.FREE_KEY_VERIFY_EMAIL_LINK + token;
        boolean locked = false;
        try {
            // 1. 根据token查询验证记录
            MailApplyRecord record = mailApplyManagerService.getByToken(token);
            if (record == null) {
                return ResponseEntity.ok(renderVerifyEmailFailurePage("验证链接无效", "该验证链接不存在，请检查验证链接。"));
            }

            // 2. 幂等性检查：验证是否已成功发放卡密，如果已经发放过则直接返回成功页面
            if (Objects.equals(record.getApplyStatus(), EmailApplyStatus.SUCCESS.getCode())) {
                return ResponseEntity.ok(renderVerifyEmailSuccessPage(record.getApplicantEmail(), record.getApplyApiKey()));
            }

            // 验证是否处于失败状态，允许重试
            if (Objects.equals(record.getApplyStatus(), EmailApplyStatus.FAILED.getCode())) {
                // 允许重试，继续执行后续流程
                log.info("检测到失败状态，允许重试，邮箱: {}, 当前状态: {}", record.getApplicantEmail(), record.getApplyStatus());
            }

            // 3. 验证是否已过期（使用最新数据）
            if (record.getVerifyExpireTime().isBefore(LocalDateTime.now())) {
                mailApplyRecordService.updateStatus(record.getId(), EmailApplyStatus.EXPIRED);
                return ResponseEntity.ok(renderVerifyEmailFailurePage("验证链接已过期", "该验证链接已过期，申请已失效"));
            }

            // 4. 获取分布式锁，防止并发重复发放卡密
            locked = redisLockUtil.lock(lockKey, 30);
            if (!locked) {
                log.warn("获取分布式锁失败，token: {}", token);
                return ResponseEntity.ok(renderVerifyEmailFailurePage("正在处理中", "验证请求正在处理中，请勿重复提交，请稍后刷新页面。"));
            }

            // 5. 增加点击次数，并修改状态为处理中
            mailApplyRecordService.increaseClickCountByToken(token);
            mailApplyRecordService.updateStatus(record.getId(), EmailApplyStatus.PROCESSING);

            // 6. 从卡密池获取卡密
            String licenseKey;
            try {
                // 判断今日是否已经发出指定数量的免费密钥
                if (mailAccountManagerService.isTodayFreeKeyLimitReached()) {
                    mailApplyRecordService.updateStatus(record.getId(), EmailApplyStatus.FAILED);
                    throw new BizException("今日免费密钥已发完了，明天早点来哦！");
                }
                licenseKey = keyinfoManagerService.getUnusedFreeKeyFromPool();
            } catch (Exception keyEx) {
                log.error("获取卡密失败，邮箱: {}, 原因: {}", record.getApplicantEmail(), keyEx.getMessage(), keyEx);
                if (!(keyEx instanceof BizException)) {
                    mailApplyRecordService.verifyApplyFail(record.getId(), EmailApplyStatus.FAILED, keyEx.getMessage());
                }
                throw keyEx;
            }

            // 7. 【事务一致】保存卡密信息并更新验证记录信息
            try {
                mailApplyManagerService.saveApiKeyAndUpdateApplyRecord(record.getId(), licenseKey);
            } catch (Exception saveEx) {
                // 事务回滚，将卡密归还到密钥池
                keyinfoManagerService.returnFreeKeyToPool(licenseKey);
                log.error("保存密钥记录失败，邮箱: {}, 卡密: {}, 原因: {}", record.getApplicantEmail(), licenseKey, saveEx.getMessage(), saveEx);
                mailApplyRecordService.verifyApplyFail(record.getId(), EmailApplyStatus.FAILED, saveEx.getMessage());
                throw new BizException("获取卡密失败，请重新尝试！");
            }

            // 8. 返回验证成功HTML页面（密钥直接展示在页面中）
            return ResponseEntity.ok(renderVerifyEmailSuccessPage(record.getApplicantEmail(), licenseKey));
        } catch (BizException e) {
            log.error("邮箱验证失败: {}", e.getMessage(), e);
            return ResponseEntity.ok(renderVerifyEmailFailurePage("验证失败", e.getMessage()));
        } catch (Exception e) {
            log.error("邮箱验证失败: {}", e.getMessage(), e);
            return ResponseEntity.ok(renderVerifyEmailFailurePage("验证失败", "验证过程中发生错误，请稍后重试。"));
        } finally {
            // 释放分布式锁
            if (locked) {
                redisLockUtil.unlock(lockKey);
            }
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
