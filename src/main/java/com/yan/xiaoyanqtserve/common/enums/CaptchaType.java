package com.yan.xiaoyanqtserve.common.enums;

import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

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
    RANDOM;

    /**
     * 根据类型名称获取验证码类型
     * <br>
     * 注意：name为空或空字符串时返回随机类型
     * @param name 类型名称（忽略大小写）
     * @return 验证码类型
     */
    @Nullable
    public static CaptchaType getByTypeName(String name) {
        if(name == null || name.isEmpty()) {
            return RANDOM;
        }
        for(CaptchaType type : CaptchaType.values()) {
            if(type.name().equalsIgnoreCase(name)) {
                return type;
            }
        }
        return null;
    }
}
