package com.yan.xiaoyanqtserve.common.exception;

import com.yan.xiaoyanqtserve.common.enums.BizStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 自定义业务异常类封装
 * @author yanchen
 */
@EqualsAndHashCode(callSuper = true)
@Data
public final class BizException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 错误码, 默认为500
     */
    private BizStatus code = BizStatus.ERROR;

    /**
     * 错误提示
     */
    private String message;


    /**
     * 空构造方法，避免反序列化问题
     */
    public BizException() {
    }

    public BizException(String message) {
        this.message = message;
    }

    public BizException(BizStatus code, String message) {
        this.message = message;
        this.code = code;
    }

    public BizException(BizStatus code, String message, Throwable cause) {
        super(cause);
        this.message = message;
        this.code = code;
    }

}