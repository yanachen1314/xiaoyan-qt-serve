package com.yan.xiaoyanqtserve.common.utils;

import com.yan.xiaoyanqtserve.security.AdminUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Spring Security 的工具类
 */
@Component
public class SecurityContextUtil {
    
    /**
     * 获取当前认证的管理员用户信息
     */
    public AdminUserDetails getCurrentAdminUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && 
            authentication.getPrincipal() instanceof AdminUserDetails) {
            return (AdminUserDetails) authentication.getPrincipal();
        }
        return null;
    }
    
    /**
     * 获取当前管理员ID
     */
    public Long getCurrentAdminId() {
        AdminUserDetails adminUser = getCurrentAdminUser();
        return adminUser != null ? adminUser.getId() : null;
    }
    
    /**
     * 获取当前管理员用户名
     */
    public String getCurrentAdminUsername() {
        AdminUserDetails adminUser = getCurrentAdminUser();
        return adminUser != null ? adminUser.getUsername() : null;
    }
    
    /**
     * 获取当前管理员角色
     */
    public String getCurrentAdminRole() {
        AdminUserDetails adminUser = getCurrentAdminUser();
        if (adminUser != null) {
            // 从 authorities 中获取角色信息
            var authorities = adminUser.getAuthorities();
            if (!authorities.isEmpty()) {
                String authority = authorities.iterator().next().getAuthority();
                // 移除 "ROLE_" 前缀，返回原始角色名
                return authority.startsWith("ROLE_") ? 
                       authority.substring(5) : authority;
            }
        }
        return null;
    }
    
    /**
     * 检查用户是否已认证
     */
    public boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && 
               authentication.isAuthenticated() && 
               !"anonymousUser".equals(authentication.getPrincipal());
    }
    
    /**
     * 检查当前用户是否为管理员（根据角色判断）
     */
    public boolean isAdmin() {
        String role = getCurrentAdminRole();
        return role != null && !role.isEmpty();
    }
    
    /**
     * 检查当前用户是否具有指定角色
     */
    public boolean hasRole(String roleName) {
        String currentRole = getCurrentAdminRole();
        return currentRole != null && currentRole.equalsIgnoreCase(roleName);
    }
    
    /**
     * 检查当前用户是否具有指定权限
     */
    public boolean hasAuthority(String authority) {
        AdminUserDetails adminUser = getCurrentAdminUser();
        if (adminUser != null) {
            return adminUser.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals(authority));
        }
        return false;
    }
    
    /**
     * 获取当前认证的原始Authentication对象
     */
    public Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }
}