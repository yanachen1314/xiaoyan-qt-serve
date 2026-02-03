package com.yan.xiaoyanqtserve.security;

import com.yan.xiaoyanqtserve.entity.AdminUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class AdminUserDetails implements UserDetails {

    private final AdminUser adminUser;

    public AdminUserDetails(AdminUser adminUser) {
        this.adminUser = adminUser;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 从数据库的 role 字段映射为 GrantedAuthority
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + adminUser.getRole()));
    }

    @Override
    public String getPassword() {
        return adminUser.getPasswordHash(); // 注意：这是哈希后的密码
    }

    @Override
    public String getUsername() {
        return adminUser.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        // 可以根据业务逻辑判断，例如检查 created_time 是否过久
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        // 可以根据业务逻辑判断，例如是否有锁定标志
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        // 可以根据业务逻辑判断，例如检查密码修改时间
        return true;
    }

    @Override
    public boolean isEnabled() {
        // 可以根据业务逻辑判断，例如是否有禁用标志
        return true;
    }

    // 可以添加 getter 方法访问 adminUser 的其他属性
    public Long getId() { return adminUser.getId(); }
}