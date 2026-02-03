package com.yan.xiaoyanqtserve.security;

import com.yan.xiaoyanqtserve.entity.AdminUser;
import com.yan.xiaoyanqtserve.mapper.AdminUserMapper;
import jakarta.annotation.Resource;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AdminUserDetailsService implements UserDetailsService {

    @Resource
    private AdminUserMapper adminUserMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AdminUser adminUser = adminUserMapper.findByUsername(username);
        if (adminUser == null) {
            throw new UsernameNotFoundException("Admin user not found: " + username);
        }
        return new AdminUserDetails(adminUser);
    }
}