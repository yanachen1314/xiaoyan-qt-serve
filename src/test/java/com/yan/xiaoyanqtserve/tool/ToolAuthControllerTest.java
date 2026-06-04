package com.yan.xiaoyanqtserve.tool;

import com.yan.xiaoyanqtserve.common.Constants;
import com.yan.xiaoyanqtserve.common.dto.tool.ToolVerifyResponse;
import com.yan.xiaoyanqtserve.common.exception.BizException;
import com.yan.xiaoyanqtserve.controller.publicApi.ToolAuthController;
import com.yan.xiaoyanqtserve.service.impl.tool.ToolAuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ToolAuthControllerTest {

    private ToolAuthService toolAuthService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        toolAuthService = mock(ToolAuthService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new ToolAuthController(toolAuthService)).build();
    }

    @Test
    @DisplayName("验证码正确时返回工具 JWT")
    void verifyCodeSuccess() throws Exception {
        when(toolAuthService.verifyAndIssueToken("doubao", "123456", "browser-fingerprint"))
                .thenReturn(new ToolVerifyResponse("jwt-token", 600, 1));

        mockMvc.perform(post("/api/public/tool-auth/verify")
                        .header(Constants.HEADER_DEVICE_ID, "browser-fingerprint")
                        .contentType("application/json")
                        .content("""
                                {
                                  "toolCode": "doubao",
                                  "code": "123456"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.toolToken").value("jwt-token"))
                .andExpect(jsonPath("$.data.expiresIn").value(600))
                .andExpect(jsonPath("$.data.remainingUses").value(1));
    }

    @Test
    @DisplayName("缺少浏览器指纹时拒绝换取工具 JWT")
    void verifyCodeMissingDeviceId() throws Exception {
        mockMvc.perform(post("/api/public/tool-auth/verify")
                        .contentType("application/json")
                        .content("""
                                {
                                  "toolCode": "doubao",
                                  "code": "123456"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("缺少浏览器指纹"));
    }

    @Test
    @DisplayName("验证码错误时返回失败响应")
    void verifyCodeFailure() throws Exception {
        when(toolAuthService.verifyAndIssueToken("doubao", "000000", "browser-fingerprint"))
                .thenThrow(new BizException("验证码无效或已过期"));

        mockMvc.perform(post("/api/public/tool-auth/verify")
                        .header(Constants.HEADER_DEVICE_ID, "browser-fingerprint")
                        .contentType("application/json")
                        .content("""
                                {
                                  "toolCode": "doubao",
                                  "code": "000000"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("验证码无效或已过期"));
    }
}
