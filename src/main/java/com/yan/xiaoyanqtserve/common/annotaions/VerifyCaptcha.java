package com.yan.xiaoyanqtserve.common.annotaions;

import com.yan.xiaoyanqtserve.common.Constants;
import com.yan.xiaoyanqtserve.common.enums.SceneIdEnum;

import java.lang.annotation.*;

/**
 * 阿里云验证码校验注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface VerifyCaptcha {
    /**
     * 请求头参数名
     */
    String headerName() default Constants.HEADER_CAPTCHA_VERIFY_PARAMS;

    /**
     * 验证场景ID
     */
    SceneIdEnum sceneId();
}