package com.yan.xiaoyanqtserve.common.dto.adminUser;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 管理员分页查询请求参数
 *
 * @author 烟尘
 * @since 2026-03-13
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AdminUserPageRequest extends PageRequest {

    /**
     * 搜索关键词（用户名）
     */
    private String username;

    /**
     * 搜索关键词（邮箱）
     */
    private String email;

    /**
     * 搜索关键词（手机号）
     */
    private String phone;
}
