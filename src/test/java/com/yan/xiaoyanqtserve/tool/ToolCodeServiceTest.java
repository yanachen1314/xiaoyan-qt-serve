package com.yan.xiaoyanqtserve.tool;

import com.yan.xiaoyanqtserve.common.Constants;
import com.yan.xiaoyanqtserve.common.exception.BizException;
import com.yan.xiaoyanqtserve.service.SysConfigService;
import com.yan.xiaoyanqtserve.service.impl.tool.ToolCodeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ToolCodeServiceTest {
    private SysConfigService sysConfigService;
    private ToolCodeService toolCodeService;

    @BeforeEach
    void setUp() {
        sysConfigService = mock(SysConfigService.class);
        toolCodeService = new ToolCodeService(sysConfigService);
    }

    @Test
    @DisplayName("从 wechat.tool.item 读取动态工具编码并完成规范化")
    void configuredToolCodeCanPassValidation() {
        when(sysConfigService.getValue(Constants.WECHAT_TOOL_ITEM_KEY)).thenReturn("""
                [
                  {"label":"custom - 自定义工具","value":"Custom"},
                  {"label":"writer - 写作工具","value":"writer"}
                ]
                """);

        assertEquals("custom", toolCodeService.requireValidCode(" CUSTOM "));
        assertEquals("writer", toolCodeService.requireValidCode("writer"));
    }

    @Test
    @DisplayName("wechat.tool.item 不存在时直接返回配置异常")
    void missingToolCodeConfigIsRejected() {
        when(sysConfigService.getValue(Constants.WECHAT_TOOL_ITEM_KEY)).thenReturn(null);

        BizException exception = assertThrows(BizException.class,
                () -> toolCodeService.requireValidCode("custom"));

        assertEquals("工具编码配置不存在: wechat.tool.item", exception.getMessage());
    }

    @Test
    @DisplayName("wechat.tool.item 格式错误时直接返回配置异常")
    void invalidToolCodeConfigIsRejected() {
        when(sysConfigService.getValue(Constants.WECHAT_TOOL_ITEM_KEY)).thenReturn("{bad json");

        BizException exception = assertThrows(BizException.class,
                () -> toolCodeService.requireValidCode("custom"));

        assertEquals("工具编码配置格式错误: wechat.tool.item", exception.getMessage());
    }

    @Test
    @DisplayName("未出现在 wechat.tool.item 的工具编码不能通过校验")
    void unknownToolCodeIsRejected() {
        when(sysConfigService.getValue(Constants.WECHAT_TOOL_ITEM_KEY)).thenReturn("""
                [
                  {"label":"custom - 自定义工具","value":"custom"}
                ]
                """);

        BizException exception = assertThrows(BizException.class,
                () -> toolCodeService.requireValidCode("fake"));

        assertEquals("工具编码无效", exception.getMessage());
    }
}
