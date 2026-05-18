package com.yan.xiaoyanqtserve.common.enums;

import lombok.Getter;
import org.springframework.lang.Nullable;

/**
 * 邮箱领取卡密状态枚举
 *
 * @author yanchen
 * @since 2026/4/30 17:33
 */
@Getter
public enum EmailApplyStatus {
    /** 待处理：记录已创建，等待用户点击验证链接 */
    PENDING((byte) 0, "待处理"),
    
    /** 处理中：用户已点击，正在验证和发放卡密 */
    PROCESSING((byte) 1, "处理中"),
    
    /** 成功：卡密已成功发放，终态 */
    SUCCESS((byte) 10, "成功"),
    
    /** 已过期：验证链接超过有效期，终态 */
    EXPIRED((byte) 20, "已过期"),
    
    /** 失败：获取或保存卡密失败，允许刷新重试 */
    FAILED((byte) 30, "失败");

    private final Byte code;

    private final String message;

    EmailApplyStatus(Byte code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * 根据状态码获取 EmailApplyStatus 枚举
     * @param code 状态码
     * @return EmailApplyStatus 枚举，未找到返回 null
     */
    @Nullable
    public static EmailApplyStatus of(Byte code) {
        for (EmailApplyStatus status : EmailApplyStatus.values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
    
    /**
     * 判断是否为终态（不可变更状态）
     * @return true-终态，false-非终态
     */
    public boolean isTerminal() {
        return this == SUCCESS || this == EXPIRED;
    }
    
    /**
     * 判断是否允许重试
     * @return true-可重试，false-不可重试
     */
    public boolean isRetryable() {
        return this == FAILED;
    }

}
