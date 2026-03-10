package com.yan.xiaoyanqtserve.common.enums;

/**
 * 系统配置类型枚举
 * @author yanchen
 * @since 2025/11/1 11:17
 */
public enum ConfigType {

    STRING("string"),
    INT("int"),
    BOOLEAN("boolean"),
    JSON( "json");

    private final String value;

    ConfigType(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    /**
     * 根据扩展名获取枚举
     * @param type 扩展名
     * @return 枚举
     */
    public static ConfigType getByType(String type) {
        for (ConfigType configType : values()) {
            if (configType.name().equals(type.toUpperCase())) {
                return configType;
            }
        }
        throw new IllegalArgumentException("不存在该配置类型: " + type);
    }
}
