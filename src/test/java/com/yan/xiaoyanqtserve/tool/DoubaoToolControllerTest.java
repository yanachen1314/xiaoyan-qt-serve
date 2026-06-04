package com.yan.xiaoyanqtserve.tool;

import com.yan.xiaoyanqtserve.common.Constants;
import com.yan.xiaoyanqtserve.common.annotaions.RateLimiter;
import com.yan.xiaoyanqtserve.common.dto.tool.DoubaoAnalyzeRequest;
import com.yan.xiaoyanqtserve.common.enums.RateLimiterType;
import com.yan.xiaoyanqtserve.common.exception.BizException;
import com.yan.xiaoyanqtserve.controller.publicApi.DoubaoToolController;
import com.yan.xiaoyanqtserve.service.impl.tool.DoubaoToolProxyService;
import com.yan.xiaoyanqtserve.service.impl.tool.ToolAuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DoubaoToolControllerTest {

    private ToolAuthService toolAuthService;
    private DoubaoToolProxyService proxyService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        toolAuthService = mock(ToolAuthService.class);
        proxyService = mock(DoubaoToolProxyService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new DoubaoToolController(toolAuthService, proxyService)).build();
    }

    @Test
    @DisplayName("图片和视频解析接口会按设备指纹共用限流配置")
    void analyzeEndpointsUseDeviceRateLimiter() throws Exception {
        assertAnalyzeRateLimiter("analyzeImages");
        assertAnalyzeRateLimiter("analyzeVideos");
    }

    @Test
    @DisplayName("图片解析接口会先校验工具 JWT 再代理到 Flask")
    void analyzeImagesConsumesTokenAndProxyToFlask() throws Exception {
        when(proxyService.analyzeImages("https://www.doubao.com/thread/demo"))
                .thenReturn(ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("""
                                {"success":true,"images":[],"errors":[]}
                                """));

        mockMvc.perform(post("/api/public/tools/doubao/images/analyze")
                        .header(Constants.HEADER_DEVICE_ID, "browser-fingerprint")
                        .header(Constants.HEADER_TOOL_TOKEN, "jwt-token")
                        .contentType("application/json")
                        .content("""
                                {
                                  "url": "https://www.doubao.com/thread/demo"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true));

        verify(toolAuthService).consumeToolToken("doubao", "browser-fingerprint", "jwt-token");
        verify(proxyService).analyzeImages("https://www.doubao.com/thread/demo");
    }

    @Test
    @DisplayName("视频解析接口会代理到 Flask 视频 API")
    void analyzeVideosProxyToFlask() throws Exception {
        when(proxyService.analyzeVideos("https://www.doubao.com/thread/demo"))
                .thenReturn(ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("""
                                {"success":true,"videos":[],"errors":[]}
                                """));

        mockMvc.perform(post("/api/public/tools/doubao/videos/analyze")
                        .header(Constants.HEADER_DEVICE_ID, "browser-fingerprint")
                        .header(Constants.HEADER_TOOL_TOKEN, "jwt-token")
                        .contentType("application/json")
                        .content("""
                                {
                                  "url": "https://www.doubao.com/thread/demo"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(toolAuthService).consumeToolToken("doubao", "browser-fingerprint", "jwt-token");
        verify(proxyService).analyzeVideos("https://www.doubao.com/thread/demo");
    }

    @Test
    @DisplayName("缺少工具 JWT 时拒绝解析")
    void rejectMissingToolToken() throws Exception {
        mockMvc.perform(post("/api/public/tools/doubao/images/analyze")
                        .header(Constants.HEADER_DEVICE_ID, "browser-fingerprint")
                        .contentType("application/json")
                        .content("""
                                {
                                  "url": "https://www.doubao.com/thread/demo"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("缺少工具令牌"));
    }

    @Test
    @DisplayName("工具 JWT 无效时拒绝解析")
    void rejectInvalidToolToken() throws Exception {
        org.mockito.Mockito.doThrow(new BizException("工具令牌无效或已使用"))
                .when(toolAuthService)
                .consumeToolToken("doubao", "browser-fingerprint", "jwt-token");

        mockMvc.perform(post("/api/public/tools/doubao/images/analyze")
                        .header(Constants.HEADER_DEVICE_ID, "browser-fingerprint")
                        .header(Constants.HEADER_TOOL_TOKEN, "jwt-token")
                        .contentType("application/json")
                        .content("""
                                {
                                  "url": "https://www.doubao.com/thread/demo"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("工具令牌无效或已使用"));
    }

    @Test
    @DisplayName("Flask 代理服务异常时返回网关错误")
    void proxyServiceFailureReturnsBadGateway() throws Exception {
        when(proxyService.analyzeImages("https://www.doubao.com/thread/demo"))
                .thenThrow(new BizException("豆包解析服务请求失败"));

        mockMvc.perform(post("/api/public/tools/doubao/images/analyze")
                        .header(Constants.HEADER_DEVICE_ID, "browser-fingerprint")
                        .header(Constants.HEADER_TOOL_TOKEN, "jwt-token")
                        .contentType("application/json")
                        .content("""
                                {
                                  "url": "https://www.doubao.com/thread/demo"
                                }
                                """))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.message").value("豆包解析服务请求失败"));
    }

    private void assertAnalyzeRateLimiter(String methodName) throws NoSuchMethodException {
        Method method = DoubaoToolController.class.getDeclaredMethod(
                methodName,
                String.class,
                String.class,
                DoubaoAnalyzeRequest.class
        );
        RateLimiter rateLimiter = method.getAnnotation(RateLimiter.class);

        assertNotNull(rateLimiter);
        assertEquals("doubaoAnalyze", rateLimiter.key());
        assertEquals(60, rateLimiter.time());
        assertEquals(10, rateLimiter.count());
        assertEquals("解析请求过于频繁，请稍后再试", rateLimiter.message());
        assertEquals(RateLimiterType.DEVICE_ID, rateLimiter.limitBy());
    }
}
