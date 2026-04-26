package com.yan.xiaoyanqtserve;

import com.yan.xiaoyanqtserve.common.enums.CaptchaType;

import java.util.Arrays;

/**
 *
 * @author yanchen
 * @since 2026/4/26 11:29
 */
public class Test {
    public static void main(String[] args) {
        System.out.println(CaptchaType.getByTypeName("SLIDER"));
        System.out.println(CaptchaType.getByTypeName("asdfdsf"));
        System.out.println(CaptchaType.getByTypeName("RANDOM"));
        System.out.println(CaptchaType.getByTypeName(""));
        System.out.println(CaptchaType.getByTypeName(null));
        System.out.println(Arrays.toString(CaptchaType.values()));

    }
}
