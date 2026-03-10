package com.yan.xiaoyanqtserve.common.enums;

import lombok.Getter;

/**
 * 密钥的产品类型枚举
 * @author yanchen
 * @since 2026/3/10 15:36
 */
@Getter
public enum ProductType {
    AI_REWRITE_BASIC("ai_rewrite_basic", "基础AIGC降重"),
    AI_REWRITE_VIP("ai_rewrite_vip", "VIP AIGC降重");

    /**
     * 类型
     */
    private final String type;
    /**
     * 类型描述信息
     */
    private final String description;

    ProductType(String type, String description) {
        this.type = type;
        this.description = description;
    }

}
