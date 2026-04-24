package com.yan.xiaoyanqtserve.controller.publicApi;

import com.yan.xiaoyanqtserve.common.Constants;
import com.yan.xiaoyanqtserve.common.annotaions.RateLimiter;
import com.yan.xiaoyanqtserve.common.dto.FreeLicenseDto;
import com.yan.xiaoyanqtserve.common.vo.FreeLicenseVo;
import com.yan.xiaoyanqtserve.common.dto.adminUser.AdminUserLoginRequest;
import com.yan.xiaoyanqtserve.common.dto.adminUser.AdminUserResponse;
import com.yan.xiaoyanqtserve.common.dto.adminUser.PasswordResetRequest;
import com.yan.xiaoyanqtserve.common.dto.key.KeyActiveDto;
import com.yan.xiaoyanqtserve.common.enums.RateLimiterType;
import com.yan.xiaoyanqtserve.common.utils.*;
import com.yan.xiaoyanqtserve.common.vo.KeyActiveVo;
import com.yan.xiaoyanqtserve.common.vo.VersionCheckVo;
import com.yan.xiaoyanqtserve.common.vo.VersionComparisonResult;
import com.yan.xiaoyanqtserve.entity.AdminUser;
import com.yan.xiaoyanqtserve.entity.FreeLicenseRecord;
import com.yan.xiaoyanqtserve.entity.KeyInfo;
import com.yan.xiaoyanqtserve.filter.SystemModeFilter;
import com.yan.xiaoyanqtserve.security.AdminUserDetails;
import com.yan.xiaoyanqtserve.service.AdminUserService;
import com.yan.xiaoyanqtserve.service.FreeLicenseService;
import com.yan.xiaoyanqtserve.service.SysConfigService;
import com.yan.xiaoyanqtserve.service.impl.KeyInfoManagerService;
import com.yan.xiaoyanqtserve.service.mail.MailService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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
    private final FreeLicenseService freeLicenseService;
    private final SysConfigService sysConfigService;

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
        String apiKeyFromHeader = MainUtil.extractApiKeyFromHeader(request);
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
    public ApiResponse<FreeLicenseVo> sendMailRoundRobin(@Valid @RequestBody FreeLicenseDto freeLicenseDto,
                                                         HttpServletRequest request) {
        String email = freeLicenseDto.getEmail();
        String deviceType = request.getHeader(Constants.HEADER_DEVICE_TYPE);
        String deviceId = request.getHeader(Constants.HEADER_DEVICE_ID);
        String clientIp = NetUtil.getClientIpAddress(request);
        String userAgent = request.getHeader("User-Agent");
        log.debug("申请邮箱：{}，设备类型：{}，设备ID：{}，客户端IP：{}，用户代理：{}", email, deviceType, deviceId, clientIp, userAgent);
        try {
            // 1. 邮箱校验
            validateEmail(email);

            // 2. 从卡密池中获取一个未使用的免费卡密
            String licenseKey = managerService.getUnusedFreeKeyFromPool();

            // 3. 发送邮件
            mailService.sendFreeLicenseEmail(email, licenseKey);

            // 4. 记录领取信息
            freeLicenseService.recordLicense(FreeLicenseRecord.builder()
                    .email(email)
                    .licenseKey(licenseKey)
                    .deviceType(deviceType)
                    .deviceId(deviceId)
                    .clientIp(clientIp)
                    .userAgent(userAgent)
                    .build()
            );

            // 5. 返回结果
            return ApiResponse.success("免费卡密发送成功，请注意查收邮件", FreeLicenseVo.builder()
                    .email(email)
                    .build());
        } catch (Exception e) {
            log.error("免费卡密领取失败: {}", e.getMessage(), e);
            // 如果是重复领取（并发情况下的唯一索引冲突）
            if (e.getMessage() != null && e.getMessage().contains("Duplicate")) {
                return ApiResponse.fail("该邮箱已领取过免费卡密，请勿重复领取");
            }
            return ApiResponse.fail(e.getMessage());
        }
    }

    /**
     * 邮箱校验
     * @param email 申请者邮箱
     */
    private void validateEmail(String email) {
        // 邮箱后缀校验
        List<String> emailSuffixList = sysConfigService.getEmailSuffixList();
        if (emailSuffixList == null || !emailSuffixList.contains("@" + email.split("@")[1])) {
            throw new IllegalArgumentException("邮箱后缀不正确");
        }
        // 检查该邮箱是否已领取
        if (freeLicenseService.hasReceived(email)) {
            throw new IllegalArgumentException("该邮箱已领取过免费卡密，请勿重复领取");
        }
    }
}
