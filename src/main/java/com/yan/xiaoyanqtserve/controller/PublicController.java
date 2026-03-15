package com.yan.xiaoyanqtserve.controller;

import com.yan.xiaoyanqtserve.common.annotaions.RateLimiter;
import com.yan.xiaoyanqtserve.common.dto.adminUser.AdminUserLoginRequest;
import com.yan.xiaoyanqtserve.common.dto.adminUser.AdminUserResponse;
import com.yan.xiaoyanqtserve.common.dto.adminUser.PasswordResetRequest;
import com.yan.xiaoyanqtserve.common.dto.key.KeyActiveDto;
import com.yan.xiaoyanqtserve.common.enums.RateLimiterType;
import com.yan.xiaoyanqtserve.common.utils.VersionCheckUtil;
import com.yan.xiaoyanqtserve.common.vo.KeyActiveVo;
import com.yan.xiaoyanqtserve.common.vo.VersionCheckVo;
import com.yan.xiaoyanqtserve.common.vo.VersionComparisonResult;
import com.yan.xiaoyanqtserve.entity.AdminUser;
import com.yan.xiaoyanqtserve.entity.KeyInfo;
import com.yan.xiaoyanqtserve.security.AdminUserDetails;
import com.yan.xiaoyanqtserve.service.AdminUserService;
import com.yan.xiaoyanqtserve.common.utils.ApiResponse;
import com.yan.xiaoyanqtserve.common.utils.JwtUtil;
import com.yan.xiaoyanqtserve.service.SysConfigService;
import com.yan.xiaoyanqtserve.service.impl.KeyInfoManagerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * 公开控制器
 * @author yanchen
 * @since 2026/3/10 15:11
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class PublicController {
    private final AdminUserService adminUserService;

    private final AuthenticationManager authenticationManager;

    private final JwtUtil jwtUtil;

    private final KeyInfoManagerService managerService;

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
    public ApiResponse<KeyActiveVo> activateKey(@Valid @RequestBody KeyActiveDto keyActiveDto) {
        // 调用 KeyInfoService 的 activateKey 方法激活密钥
        KeyInfo keyInfo = managerService.activateKey(keyActiveDto);

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
     * 自动检查版本接口
     *
     * @param version 客户端当前版本，格式如 "v1.0.0" 或 "1.0.0"
     * @return 版本检查结果
     */
    @GetMapping("/check/{version}")
    public ApiResponse<VersionCheckVo> autoCheckVersion(@PathVariable("version") String version) {
        // 1. 获取服务器最新版本
        String versionKey = "system.version";
        String latestVersion = sysConfigService.getValue(versionKey);

        String linkKey = "system.updateLink";
        String linkUrl = sysConfigService.getValue(linkKey);

        if (latestVersion == null || latestVersion.trim().isEmpty()) {
            log.error("System config key '{}' is missing or empty.", versionKey);
            return ApiResponse.success(VersionCheckVo.builder()
                    .version("v0.0.0")
                    .currentVersion(version)
                    .latest(true)
                    .updateRequired(false)
                    .message("服务器版本信息未配置，请联系管理员")
                    .linkUrl(linkUrl)
                    .build());
        }

        // 2. 比较版本
        VersionComparisonResult result = VersionCheckUtil.compareVersions(version, latestVersion);

        // 3. 构建响应
        VersionCheckVo vo = VersionCheckVo.builder()
                .version(latestVersion)
                .currentVersion(version)
                .latest(result.isLatest())
                .updateRequired(result.isUpdateRequired())
                .message(result.getMessage())
                .linkUrl(linkUrl)
                .build();

        return ApiResponse.success(vo);
    }


    /**
     * 管理员登录接口
     *
     * @param loginRequest 登录请求参数
     * @return 登录结果
     */
    @PostMapping("/auth/login")
    public ApiResponse<AdminUserResponse> login(@Valid @RequestBody AdminUserLoginRequest loginRequest) {
        try {
            // 使用 Spring Security 进行身份验证
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()));

            // 获取用户信息
            AdminUserDetails userDetails = (AdminUserDetails) authentication.getPrincipal();
            AdminUser adminUser = adminUserService.getById(userDetails.getId());

            // 更新最后登录时间
            adminUser.setLastLoginTime(LocalDateTime.now());
            adminUserService.updateById(adminUser);

            // 生成 JWT 令牌
            String token = jwtUtil.generateToken(adminUser.getUsername());

            // 构造响应数据
            AdminUserResponse response = new AdminUserResponse();
            response.setId(adminUser.getId());
            response.setUsername(adminUser.getUsername());
            response.setToken(token);
            response.setRole(adminUser.getRole());
            response.setEmail(adminUser.getEmail());
            response.setPhone(adminUser.getPhone());

            return ApiResponse.success(response);
        } catch (Exception e) {
            log.error("登录失败：{}", e.getMessage());
            return ApiResponse.fail("用户名或密码错误");
        }
    }

    /**
     * 忘记密码 - 重置密码接口
     * <p>
     * 根据手机号或邮箱验证用户身份，验证通过后重置密码
     *
     * @param resetRequest 密码重置请求参数
     * @return 重置结果
     */
    @PostMapping("/auth/reset")
    public ApiResponse<Void> resetPassword(@Valid @RequestBody PasswordResetRequest resetRequest) {
        try {
            // 调用 AdminUserService 的 resetPasswordByContact 方法重置密码
            boolean success = adminUserService.resetPasswordByContact(
                    resetRequest.getContact(),
                    resetRequest.getNewPassword()
            );

            if (success) {
                return ApiResponse.success("密码重置成功", null);
            } else {
                return ApiResponse.fail("未找到匹配的手机号或邮箱");
            }
        } catch (Exception e) {
            log.error("密码重置失败：{}", e.getMessage());
            return ApiResponse.fail("密码重置失败");
        }
    }

}
