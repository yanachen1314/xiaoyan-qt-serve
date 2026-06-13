package com.yan.xiaoyanqtserve.tool;

import com.yan.xiaoyanqtserve.common.enums.ToolCodeEnum;
import com.yan.xiaoyanqtserve.common.exception.BizException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ToolCodeEnumTest {

    @Test
    @DisplayName("已配置的工具编码可以通过枚举校验")
    void configuredToolCodeCanPassValidation() {
        assertEquals("doubao", ToolCodeEnum.requireValidCode(" doubao "));
    }

    @Test
    @DisplayName("空工具编码不能通过枚举校验")
    void blankToolCodeIsRejected() {
        BizException exception = assertThrows(BizException.class, () -> ToolCodeEnum.requireValidCode(" "));

        assertEquals("工具编码不能为空", exception.getMessage());
    }

    @Test
    @DisplayName("未配置的工具编码不能通过枚举校验")
    void unknownToolCodeIsRejected() {
        BizException exception = assertThrows(BizException.class, () -> ToolCodeEnum.requireValidCode("fake"));

        assertEquals("工具编码无效", exception.getMessage());
    }
}
