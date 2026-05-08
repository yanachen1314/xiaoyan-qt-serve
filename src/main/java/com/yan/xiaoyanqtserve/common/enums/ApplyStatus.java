package com.yan.xiaoyanqtserve.common.enums;

import lombok.Getter;

/**
 * 邮箱领取卡密状态
 *
 * @author yanchen
 * @since 2026/4/30 17:33
 */
@Getter
public enum ApplyStatus {
    NOT_VERIFIED((byte) 0, "未验证"),
    HAS_SENT((byte) 1, "已发送卡密"),
    HAS_SENT_FAILED((byte) 2, "发送卡密失败");

    private final Byte code;

    private final String message;

    ApplyStatus(Byte code, String message) {
        this.code = code;
        this.message = message;
    }

}
