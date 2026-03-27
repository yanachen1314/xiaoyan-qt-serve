package com.yan.xiaoyanqtserve.service.impl.sys;

import com.yan.xiaoyanqtserve.entity.AdminUser;
import com.yan.xiaoyanqtserve.mapper.AdminUserMapper;
import com.yan.xiaoyanqtserve.service.AdminUserService;
import com.yan.xiaoyanqtserve.common.dto.adminUser.AdminUserUpdateRequest;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import jakarta.annotation.Resource;
import java.time.LocalDateTime;

/**
 * <p>
 * 管理员用户表 服务实现类
 * </p>
 *
 * @author 烟尘
 * @since 2026-02-03
 */
@Service
public class AdminUserServiceImpl extends ServiceImpl<AdminUserMapper, AdminUser> implements AdminUserService {

    @Resource
    private PasswordEncoder passwordEncoder;

    @Override
    public AdminUser findByUsername(String username) {
        return baseMapper.findByUsername(username);
    }

    @Override
    public AdminUser findById(Long id) {
        return getById(id);
    }

    @Override
    public boolean updateAdminUser(AdminUser adminUser) {
        return baseMapper.updateById(adminUser) > 0;
    }

    @Override
    public boolean updateAdminUserByRequest(AdminUserUpdateRequest updateRequest) {
        // 检查管理员是否存在
        AdminUser existingUser = getById(updateRequest.getId());
        if (existingUser == null) {
            return false;
        }

        // 如果更新用户名，检查是否与其他用户冲突
        if (updateRequest.getUsername() != null && !updateRequest.getUsername().equals(existingUser.getUsername())) {
            AdminUser userWithSameName = getOne(
                    new QueryWrapper<AdminUser>()
                            .eq("username", updateRequest.getUsername())
                            .ne("id", updateRequest.getId())
            );
            if (userWithSameName != null) {
                return false;
            }
            existingUser.setUsername(updateRequest.getUsername());
        }

        // 如果更新密码，则加密存储
        if (updateRequest.getPassword() != null) {
            existingUser.setPasswordHash(passwordEncoder.encode(updateRequest.getPassword()));
        }

        // 更新其他字段
        if (updateRequest.getEmail() != null) {
            existingUser.setEmail(updateRequest.getEmail());
        }
        if (updateRequest.getPhone() != null) {
            existingUser.setPhone(updateRequest.getPhone());
        }
        
        existingUser.setUpdatedTime(LocalDateTime.now());

        // 保存更新
        return updateAdminUser(existingUser);
    }

    @Override
    public boolean deleteAdminUser(Long id) {
        return baseMapper.deleteById(id) > 0;
    }

    @Override
    public boolean resetPasswordByContact(String contact, String newPassword) {
        // 根据手机号或邮箱查找用户
        QueryWrapper<AdminUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.and(wrapper -> 
            wrapper.eq("phone", contact).or().eq("email", contact)
        );
        
        AdminUser adminUser = getOne(queryWrapper);
        
        if (adminUser == null) {
            return false;
        }
        
        // 更新密码
        adminUser.setPasswordHash(passwordEncoder.encode(newPassword));
        adminUser.setUpdatedTime(LocalDateTime.now());
        
        return updateAdminUser(adminUser);
    }
}
