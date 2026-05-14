package com.yan.xiaoyanqtserve.common.dto.adminUser;

import lombok.*;

/**
 * 邮件账户分页请求参数
 *
 * @author 烟尘
 * @since 2026-05-14
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MailAccountPageRequest extends PageRequest {
    /**
     * 邮箱地址
     */
    private String emailAddress;

    /**
     * 状态：0-禁用，1-启用
     */
    private Byte status;

}
