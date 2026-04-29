package com.yan.xiaoyanqtserve.common.dto;

import lombok.Data;

@Data
public class CaptchaVerifyVo {
    // 验证码结果
    private Boolean captchaResult;
    // 业务结果
    private Object bizResult;

    public static <T> CaptchaVerifyVo buildFail(T bizResult) {
        CaptchaVerifyVo captchaVerifyVo = new CaptchaVerifyVo();
        captchaVerifyVo.setCaptchaResult(false);
        captchaVerifyVo.setBizResult(bizResult);
        return captchaVerifyVo;
    }
    public static <T> CaptchaVerifyVo buildSuccess(T bizResult) {
        CaptchaVerifyVo captchaVerifyVo = new CaptchaVerifyVo();
        captchaVerifyVo.setCaptchaResult(true);
        captchaVerifyVo.setBizResult(bizResult);
        return captchaVerifyVo;
    }
}