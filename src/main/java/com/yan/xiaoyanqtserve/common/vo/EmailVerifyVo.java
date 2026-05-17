package com.yan.xiaoyanqtserve.common.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 邮箱验证返回VO
 * @author yanchen
 * @since 2026/4/30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailVerifyVo {
    
    /**
     * 邮箱地址
     */
    private String email;
    
    /**
     * 提示信息
     */
    private String message;
}
