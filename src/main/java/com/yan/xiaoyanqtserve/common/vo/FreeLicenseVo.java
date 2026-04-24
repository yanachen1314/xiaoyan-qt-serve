package com.yan.xiaoyanqtserve.common.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 免费密钥申请VO
 * @author yanchen
 * @since 2026/4/22 22:29
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FreeLicenseVo {
    /**
     * 申请者邮箱
     */
    private String email;
    /**
     * 申请的免费密钥
     */
    private String applyLicenseKey;
}
