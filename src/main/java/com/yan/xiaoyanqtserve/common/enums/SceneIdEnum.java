package com.yan.xiaoyanqtserve.common.enums;

/**
 * 场景ID枚举
 * @author yanchen
 * @since 2025/11/1 11:17
 */
public enum SceneIdEnum {

    EMAIL_APPLY("f1yi7e7j");

    private final String value;

    SceneIdEnum(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    /**
     * 根据场景ID获取枚举
     * @param sceneId 场景ID
     * @return 枚举
     */
    public static SceneIdEnum getBySceneId(String sceneId) {
        for (SceneIdEnum sceneIdEnum : values()) {
            if (sceneIdEnum.value().equals(sceneId)) {
                return sceneIdEnum;
            }
        }
        throw new IllegalArgumentException("不存在该场景ID: " + sceneId);
    }
}
