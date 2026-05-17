package com.yan.xiaoyanqtserve.controller.publicApi;

import com.yan.xiaoyanqtserve.common.Constants;
import com.yan.xiaoyanqtserve.common.dto.adminUser.AdminUserLoginRequest;
import com.yan.xiaoyanqtserve.common.dto.adminUser.AdminUserResponse;
import com.yan.xiaoyanqtserve.common.dto.adminUser.PasswordResetRequest;
import com.yan.xiaoyanqtserve.common.utils.ApiResponse;
import com.yan.xiaoyanqtserve.common.utils.JwtUtil;
import com.yan.xiaoyanqtserve.common.utils.VersionCheckUtil;
import com.yan.xiaoyanqtserve.common.vo.VersionCheckVo;
import com.yan.xiaoyanqtserve.common.vo.VersionComparisonResult;
import com.yan.xiaoyanqtserve.entity.AdminUser;
import com.yan.xiaoyanqtserve.filter.SystemModeFilter;
import com.yan.xiaoyanqtserve.security.AdminUserDetails;
import com.yan.xiaoyanqtserve.service.AdminUserService;
import com.yan.xiaoyanqtserve.service.SysConfigService;
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
 * 系统公开控制器
 * @author yanchen
 * @since 2026/3/10 15:11
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class SystemController {
    private final AdminUserService adminUserService;

    private final AuthenticationManager authenticationManager;

    private final JwtUtil jwtUtil;

    private final SysConfigService sysConfigService;


    /**
     * 获取可申请的邮箱后缀
     *
     * @return 可申请的邮箱后缀JSON字符串
     */
    @GetMapping("/email/apply-suffix")
    public ApiResponse<List<String>> applyEmailSuffix() {
        return ApiResponse.success(sysConfigService.getEmailSuffixList());
    }

    /**
     * 自动检查版本接口
     *
     * @param version 客户端当前版本，格式如 "v1.0.0" 或 "1.0.0"
     * @return 版本检查结果
     */
    @Deprecated
    @GetMapping("/check/version/{version}")
    public ApiResponse<VersionCheckVo> autoCheckVersion(@PathVariable("version") String version) {
        // 1. 获取服务器最新版本
        String latestVersion = sysConfigService.getValue(Constants.VERSION_KEY);
        String linkUrl = sysConfigService.getValue(Constants.LINK3_KEY);

        if (latestVersion == null || latestVersion.trim().isEmpty()) {
            log.info("System config key '{}' is missing or empty.", Constants.VERSION_KEY);
            return ApiResponse.success(VersionCheckVo.builder()
                    .version("v0.0.0")
                    .clientVersion(version)
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
                .clientVersion(version)
                .latest(result.isLatest())
                .updateRequired(result.isUpdateRequired())
                .message(result.getMessage())
                .linkUrl(linkUrl)
                .build();

        return ApiResponse.success(vo);
    }

    /**
     * 检查系统维护状态
     * @see SystemModeFilter
     * @return 系统维护状态。 false 示系统正常运行，true 示系统维护中
     */
    @GetMapping("/check/maintenance")
    public ApiResponse<Map<String, Object>> checkMaintenance() {
        return ApiResponse.success("系统运行正常", Map.of("status", false));
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
            log.info("登录失败：{}", e.getMessage());
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
            log.info("密码重置失败：{}", e.getMessage());
            return ApiResponse.fail("密码重置失败");
        }
    }

}
