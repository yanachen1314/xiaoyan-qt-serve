package com.yan.xiaoyanqtserve.controller;

import com.yan.xiaoyanqtserve.common.annotaions.RateLimiter;
import com.yan.xiaoyanqtserve.common.dto.adminUser.AdminUserLoginRequest;
import com.yan.xiaoyanqtserve.common.dto.adminUser.AdminUserResponse;
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
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
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
public class PublicController {
    @Resource
    private AdminUserService adminUserService;

    @Resource
    private AuthenticationManager authenticationManager;

    @Resource
    private PasswordEncoder passwordEncoder;

    @Resource
    private JwtUtil jwtUtil;

    @Resource
    private KeyInfoManagerService managerService;

    @Resource
    private SysConfigService sysConfigService;

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
        return ApiResponse.success("密钥激活成功",
                KeyActiveVo.builder()
                        .apiKey(keyInfo.getKeyValue())
                        .device(keyInfo.getActivationDevice())
                        .expireTime(keyInfo.getExpiryTime())
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
            // 使用Spring Security进行身份验证
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

            // 生成JWT令牌
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
            log.error("登录失败: {}", e.getMessage());
            return ApiResponse.fail("用户名或密码错误");
        }
    }

}
