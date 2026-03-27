package com.yan.xiaoyanqtserve;

/**
 *
 * @author yanchen
 * @since 2026/3/27 21:48
 */
public class Test {
    public static void main(String[] args) {
        System.out.println(convertEquityTime(null));
        System.out.println(convertEquityTime(0));
        System.out.println(convertEquityTime(59));
        System.out.println(convertEquityTime(60));
        System.out.println(convertEquityTime(61));
        System.out.println(convertEquityTime(1439));
        System.out.println(convertEquityTime(1440));
        System.out.println(convertEquityTime(1441));
        System.out.println(convertEquityTime(2879));
        System.out.println(convertEquityTime(2880));
        System.out.println(convertEquityTime(2881));
        System.out.println(convertEquityTime(2981));
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
