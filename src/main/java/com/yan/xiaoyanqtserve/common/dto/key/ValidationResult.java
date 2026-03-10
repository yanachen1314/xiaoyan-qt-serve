package com.yan.xiaoyanqtserve.common.dto.key;

import com.yan.xiaoyanqtserve.common.enums.BizStatus;
import com.yan.xiaoyanqtserve.entity.KeyInfo;
import lombok.Getter;


/**
 * 密钥验证结果
 */
@Getter
public class ValidationResult {
    private final boolean success;
    private final KeyInfo keyInfo;
    private final String errorMessage;
    private final BizStatus errorCode;

    private ValidationResult(boolean success, KeyInfo keyInfo, String errorMessage, BizStatus errorCode) {
        this.success = success;
        this.keyInfo = keyInfo;
        this.errorMessage = errorMessage;
        this.errorCode = errorCode;
    }

    public static ValidationResult success(KeyInfo keyInfo) {
        return new ValidationResult(true, keyInfo, null, BizStatus.KEY_VALID);
    }

    public static ValidationResult failure(String errorMessage, BizStatus errorCode) {
        return new ValidationResult(false, null, errorMessage, errorCode);
    }

    public static ValidationResult failure(BizStatus errorCode) {
        return new ValidationResult(false, null, null, errorCode);
    }

}