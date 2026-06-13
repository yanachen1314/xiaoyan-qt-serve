package com.yan.xiaoyanqtserve.tool;

import com.yan.xiaoyanqtserve.common.Constants;
import com.yan.xiaoyanqtserve.common.dto.tool.ToolVerifyResponse;
import com.yan.xiaoyanqtserve.controller.main.MainToolAuthController;
import com.yan.xiaoyanqtserve.service.impl.tool.ToolAuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MainToolAuthControllerTest {

    private ToolAuthService toolAuthService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        toolAuthService = mock(ToolAuthService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new MainToolAuthController(toolAuthService)).build();
    }

    @Test
    @DisplayName("验证码换令牌前会校验工具编码枚举")
    void verifyChecksToolCodeEnumBeforeIssuingToken() throws Exception {
        when(toolAuthService.verifyAndIssueToken("doubao", "123456", "browser-fingerprint"))
                .thenReturn(new ToolVerifyResponse("jwt-token", 600, 0));

        mockMvc.perform(post("/api/main/tool-auth/verify")
                        .header(Constants.HEADER_DEVICE_ID, "browser-fingerprint")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"toolCode":"doubao","code":"123456"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.toolToken").value("jwt-token"));

        verify(toolAuthService).verifyAndIssueToken("doubao", "123456", "browser-fingerprint");
    }

    @Test
    @DisplayName("未配置到枚举的工具编码不会发放令牌")
    void verifyRejectsUnknownToolCode() throws Exception {
        mockMvc.perform(post("/api/main/tool-auth/verify")
                        .header(Constants.HEADER_DEVICE_ID, "browser-fingerprint")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"toolCode":"fake","code":"123456"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("工具编码无效"));

        verify(toolAuthService, never()).verifyAndIssueToken("fake", "123456", "browser-fingerprint");
    }

    @Test
    @DisplayName("校验工具令牌前会校验工具编码枚举")
    void validateTokenChecksToolCodeEnumBeforeConsumingToken() throws Exception {
        mockMvc.perform(post("/api/main/tool-auth/token/validate")
                        .param("toolCode", "doubao")
                        .header(Constants.HEADER_DEVICE_ID, "browser-fingerprint")
                        .header(Constants.HEADER_TOOL_TOKEN, "jwt-token"))
                .andExpect(status().isOk());

        verify(toolAuthService).consumeToolToken("doubao", "browser-fingerprint", "jwt-token");
    }
}
