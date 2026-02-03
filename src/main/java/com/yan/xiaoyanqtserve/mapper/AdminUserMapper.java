package com.yan.xiaoyanqtserve.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yan.xiaoyanqtserve.entity.AdminUser;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 管理员用户表 Mapper 接口
 * </p>
 *
 * @author 烟尘
 * @since 2026-02-03
 */
@Mapper
public interface AdminUserMapper extends BaseMapper<AdminUser> {

    /**
     * 根据用户名查询用户
     * @param username 用户账号
     * @return  用户
     */
    AdminUser findByUsername(String username);
}
