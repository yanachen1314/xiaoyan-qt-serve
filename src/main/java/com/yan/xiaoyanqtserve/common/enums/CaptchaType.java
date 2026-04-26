package com.yan.xiaoyanqtserve.common.enums;

import org.springframework.lang.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * 验证码类型枚举
 * @see cloud.tianai.captcha.common.constant.CaptchaTypeConstant 官方验证码类型常量
 * @author yanchen
 * @since 2026/4/25 21:10
 */
public enum CaptchaType {

    /**
     * 随机类型的验证码
     */
    RANDOM,

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
    WORD_IMAGE_CLICK;

    // 缓存非 RANDOM 的类型列表，避免每次调用都重新过滤
    private static final List<CaptchaType> REAL_TYPES =
            Arrays.stream(CaptchaType.values())
                    .filter(type -> type != RANDOM)
                    .toList();

    private static final Random random = new Random();

    /**
     * 根据类型名称获取验证码类型
     * <br>
     * 注意：name为空或空字符串时返回随机类型；若匹配到 RANDOM 也返回随机类型
     * @param name 类型名称（忽略大小写）
     * @return 验证码类型
     */
    @Nullable
    public static CaptchaType getByTypeName(String name) {
        if(name == null || name.isEmpty()) {
            return getRandomType();
        }
        for(CaptchaType type : CaptchaType.values()) {
            if(type.name().equalsIgnoreCase(name)) {
                if(type != RANDOM)
                    return type;
                return getRandomType();
            }
        }
        return null;
    }

    /**
     * 获取随机的验证码类型
     * <br>
     * 注意：返回的验证码类型不会是 {@code RANDOM}
     * @return 随机的验证码类型
     */
    private static CaptchaType getRandomType() {
        int randomIndex = random.nextInt(REAL_TYPES.size());
        return REAL_TYPES.get(randomIndex);
    }
}
