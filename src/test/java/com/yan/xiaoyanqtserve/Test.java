package com.yan.xiaoyanqtserve;

import com.yan.xiaoyanqtserve.common.enums.RequestType;

import java.security.SecureRandom;
import java.util.Base64;

/**
 *
 * @author yanchen
 * @since 2026/3/27 21:48
 */
public class Test {
    public static void main(String[] args) {
    }

    // 生成更安全的token
    public static String generateSecureToken() {
        byte[] randomBytes = new byte[64];
        new SecureRandom().nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    /**
     * 将权益时间转换为字符串
     * @param equityTime 权益时间（单位：分钟），null值会被视为0
     * @return 权益时间字符串，如"30分钟"、"2小时"、"3天"
     */
    private static String convertEquityTime(Integer equityTime) {
        // 处理null值情况
        if (equityTime == null || equityTime <= 0) {
            return "0分钟";
        }

        if (equityTime < 60) {
            return equityTime + "分钟";
        } else if (equityTime < 1440) { // 1440分钟 = 24小时
            int hours = equityTime / 60;
            int remainingMinutes = equityTime % 60;
            if (remainingMinutes > 0) {
                return hours + "小时" + remainingMinutes + "分钟";
            } else {
                return hours + "小时";
            }
        } else { // 1440分钟 = 1天
            int days = equityTime / 1440;
            int remainingMinutes = equityTime % 1440;
            if (remainingMinutes > 0) {
                int hours = remainingMinutes / 60;
                int finalMinutes = remainingMinutes % 60;
                if (finalMinutes > 0) {
                    return days + "天" + hours + "小时" + finalMinutes + "分钟";
                } else {
                    return days + "天" + hours + "小时";
                }
            } else {
                return days + "天";
            }
        }
    }
}
