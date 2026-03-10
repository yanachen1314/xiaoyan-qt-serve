package com.yan.xiaoyanqtserve.common.dto.adminUser;

import lombok.Data;

/**
 * 管理员删除请求DTO
 * 包含删除管理员所需的ID字段
 */
@Data
public class AdminUserDeleteRequest {

    /**
     * 管理员ID
     * 不能为空
     */
    private Long id;
}
