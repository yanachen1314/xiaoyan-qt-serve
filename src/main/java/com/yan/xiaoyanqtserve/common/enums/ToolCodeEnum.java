package com.yan.xiaoyanqtserve.common.enums;

import com.yan.xiaoyanqtserve.common.exception.BizException;
import org.springframework.util.StringUtils;

import java.util.Arrays;

/**
 * 官方工具编码枚举。
 */
public enum ToolCodeEnum {
    DOUBAO("doubao");

    private final String code;

    ToolCodeEnum(String code) {
        this.code = code;
    }

    public String code() {
        return code;
    }

    public static String requireValidCode(String toolCode) {
        if (!StringUtils.hasText(toolCode)) {
            throw new BizException(BizStatus.BAD_REQUEST, "工具编码不能为空");
        }

        String normalizedToolCode = toolCode.trim();
        return Arrays.stream(values())
                .map(ToolCodeEnum::code)
                .filter(normalizedToolCode::equals)
                .findFirst()
                .orElseThrow(() -> new BizException(BizStatus.BAD_REQUEST, "工具编码无效"));
    }
}
