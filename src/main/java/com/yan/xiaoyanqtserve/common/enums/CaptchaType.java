package com.yan.xiaoyanqtserve.common.enums;

/**
 * 验证码类型枚举
 * @see cloud.tianai.captcha.common.constant.CaptchaTypeConstant 官方验证码类型常量
 * @author yanchen
 * @since 2026/4/25 21:10
 */
public enum CaptchaType {
    /**
     * 滑块验证
     */
    SLIDER,
    /**
     * 旋转验证
     */
    ROTATE,
    /**
     * 文字图片点选
     */
    WORD_IMAGE_CLICK,

    /**
     * 随机类型的验证码
     */
    RANDOM
}
