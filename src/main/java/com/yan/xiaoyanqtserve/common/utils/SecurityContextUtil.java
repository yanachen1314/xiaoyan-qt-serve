package com.yan.xiaoyanqtserve.common.utils;

import com.yan.xiaoyanqtserve.common.enums.Role;
import com.yan.xiaoyanqtserve.entity.KeyInfo;
import com.yan.xiaoyanqtserve.security.AdminUserDetails;
import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

/**
 * Spring Security 的工具类
 */
@Component
public class SecurityContextUtil {
    private Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    /**
     * 获取当前登录的 API Key 用户
     * @return 认证对象
     */
    @Nullable
    public KeyInfo getApiKeyUser() {
        Authentication auth = getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof KeyInfo keyInfo) {
            return keyInfo;
        }
        return null;
    }

    /**
     * 判断当前用户是否是 API Key 用户
     * @return 是否是 API Key 用户
     */
    public Boolean isApiKeyUser() {
        Authentication auth = getAuthentication();
        return auth != null && auth.getAuthorities()
                        .stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_"+ Role.KEY_USER));
    }

    /**
     * 获取当前登录的 Admin 用户
     * @return 认证对象
     */
    @Nullable
    public AdminUserDetails getAdminUser() {
        Authentication auth = getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserDetails user) {
            return (AdminUserDetails) user;
        }
        return null;
    }

    /**
     * 判断当前用户是否是管理员
     * @return 是否是管理员
     */
    public Boolean isAdmin() {
        Authentication auth = getAuthentication();
        return auth != null && auth.getAuthorities().stream().anyMatch(
                a -> a.getAuthority().equals("ROLE_"+ Role.ADMIN));
    }
}