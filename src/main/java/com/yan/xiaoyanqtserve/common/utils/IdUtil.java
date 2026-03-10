package com.yan.xiaoyanqtserve.common.utils;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.lang.UUID;

/**
 * 封装 hutool UUID、雪花算法工具类
 * @author yanchen
 * @since 2025/2/15 13:00
 */
public class IdUtil {
    private static volatile Snowflake SNOWFLAKE;

    private IdUtil() {
        // 私有构造函数，防止实例化
    }

    public static Snowflake getInstance() {
        if (SNOWFLAKE == null) {
            synchronized (IdUtil.class) {
                if (SNOWFLAKE == null) {
                    SNOWFLAKE = cn.hutool.core.util.IdUtil.getSnowflake();
                }
            }
        }
        return SNOWFLAKE;
    }

    /**
     * 获取雪花算法ID
     * <p>
     * huTool基于Twitter的Snowflake算法实现
     * @return 字符串——雪花算法ID
     */
    public static String getSnowIdStr() {
        return getInstance().nextIdStr();
    }


    /**
     * 简单模式获取UUID
     * @return 不带'-'的UUID字符串
     */
    public static String getUUIDSimple() {
        return  UUID.fastUUID().toString( true);
    }

    /**
     * 默认模式获取UUID
     * @return 带'-'的UUID字符串
     */
    public static String getUUID() {
        return  UUID.fastUUID().toString( false);
    }
}
