package com.yan.xiaoyanqtserve.common.dto.key;

import com.yan.xiaoyanqtserve.entity.KeyInfo;
import lombok.Getter;


/**
 * 密钥验证结果
 */
@Getter
public class ValidationResult {
    private boolean success;
    private KeyInfo keyInfo;
    private String errorMessage;
    private ValidationErrorCode errorCode;

    public enum ValidationErrorCode {
        KEY_NOT_FOUND, //密钥不存在
        KEY_FORMAT_INVALID, //密钥格式非法
        KEY_INACTIVE, //密钥未启用
        KEY_EXPIRED, //密钥已过期
        ACTIVATIONS_EXHAUSTED, //密钥激活次数已用完
        USES_EXHAUSTED, //密钥功能使用次数已用完
        VALID //密钥验证成功
    }

    private ValidationResult(boolean success, KeyInfo keyInfo, String errorMessage, ValidationErrorCode errorCode) {
        this.success = success;
        this.keyInfo = keyInfo;
        this.errorMessage = errorMessage;
        this.errorCode = errorCode;
    }

    public static ValidationResult success(KeyInfo keyInfo) {
        return new ValidationResult(true, keyInfo, null, ValidationErrorCode.VALID);
    }

    public static ValidationResult failure(String errorMessage, ValidationErrorCode errorCode) {
        return new ValidationResult(false, null, errorMessage, errorCode);
    }

}