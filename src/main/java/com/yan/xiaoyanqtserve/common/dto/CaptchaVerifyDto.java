package com.yan.xiaoyanqtserve.common.dto;

import lombok.Data;

/**
 * 校验验证码 请求参数DTO
 * @author yanchen
 * @since 2026/4/27 14:03
 */
@Data
public class CaptchaVerifyDto {

    /**
     * 验证码参数(阿里云验证码服务端返回的验证码参数)
     */
    private String captchaVerifyParam;

    // 业务参数
    private BizParam bizParam;

    @Data
    public static class BizParam {

    }
}
