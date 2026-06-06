package com.yan.xiaoyanqtserve.controller.main;

import com.yan.xiaoyanqtserve.common.Constants;
import com.yan.xiaoyanqtserve.common.annotaions.RateLimiter;
import com.yan.xiaoyanqtserve.common.dto.tool.DoubaoAnalyzeRequest;
import com.yan.xiaoyanqtserve.common.enums.BizStatus;
import com.yan.xiaoyanqtserve.common.enums.RateLimiterType;
import com.yan.xiaoyanqtserve.common.utils.ApiResponse;
import com.yan.xiaoyanqtserve.service.impl.tool.DoubaoToolProxyService;
import com.yan.xiaoyanqtserve.service.impl.tool.ToolAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

/**
 * 【主站】豆包工具接口
 * 提供豆包图片和视频解析接口
 *
 * @author yanchen
 * @since 2026-06-06
 */
@RestController
@RequestMapping("/api/main/tools/doubao")
@RequiredArgsConstructor
public class MainDoubaoController {
    private static final String TOOL_CODE = "doubao";
    private static final String ANALYZE_RATE_LIMIT_KEY = "doubaoAnalyze";

    private final ToolAuthService toolAuthService;
    private final DoubaoToolProxyService proxyService;

    /**
     * 解析豆包图片素材
     *
     * @param deviceId  浏览器指纹
     * @param toolToken 工具JWT
     * @param request   解析请求
     * @return 图片解析响应
     */
    @RateLimiter(
            key = ANALYZE_RATE_LIMIT_KEY,
            time = 60,
            count = 10,
            message = "解析请求过于频繁，请稍后再试",
            limitBy = RateLimiterType.DEVICE_ID
    )
    @PostMapping(value = "/images/analyze", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> analyzeImages(
            @RequestHeader(value = Constants.HEADER_DEVICE_ID, required = false) String deviceId,
            @RequestHeader(value = Constants.HEADER_TOOL_TOKEN, required = false) String toolToken,
            @RequestBody DoubaoAnalyzeRequest request) {
        return analyze(deviceId, toolToken, request, true);
    }

    /**
     * 解析豆包视频素材
     *
     * @param deviceId  浏览器指纹
     * @param toolToken 工具JWT
     * @param request   解析请求
     * @return 视频解析响应
     */
    @RateLimiter(
            key = ANALYZE_RATE_LIMIT_KEY,
            time = 60,
            count = 10,
            message = "解析请求过于频繁，请稍后再试",
            limitBy = RateLimiterType.DEVICE_ID
    )
    @PostMapping(value = "/videos/analyze", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> analyzeVideos(
            @RequestHeader(value = Constants.HEADER_DEVICE_ID, required = false) String deviceId,
            @RequestHeader(value = Constants.HEADER_TOOL_TOKEN, required = false) String toolToken,
            @RequestBody DoubaoAnalyzeRequest request) {
        return analyze(deviceId, toolToken, request, false);
    }

    /**
     * 解析处理
     *
     * @param deviceId  浏览器指纹
     * @param toolToken 工具JWT
     * @param request   解析请求
     * @param imageMode 是否为图片模式
     * @return 解析响应
     */
    private ResponseEntity<?> analyze(String deviceId, String toolToken, DoubaoAnalyzeRequest request, boolean imageMode) {
        if (!StringUtils.hasText(deviceId)) {
            return fail(HttpStatus.BAD_REQUEST, "缺少浏览器指纹");
        }
        if (!StringUtils.hasText(toolToken)) {
            return fail(HttpStatus.UNAUTHORIZED, "缺少工具令牌");
        }
        if (request == null || !StringUtils.hasText(request.url())) {
            return fail(HttpStatus.BAD_REQUEST, "url 不能为空");
        }

        try {
            toolAuthService.consumeToolToken(TOOL_CODE, deviceId, toolToken);
        } catch (Exception e) {
            return fail(HttpStatus.UNAUTHORIZED, BizStatus.UNAUTHORIZED, e.getMessage());
        }

        try {
            ResponseEntity<String> response = imageMode
                    ? proxyService.analyzeImages(request.url())
                    : proxyService.analyzeVideos(request.url());
            MediaType contentType = response.getHeaders().getContentType();
            ResponseEntity.BodyBuilder builder = ResponseEntity.status(response.getStatusCode());
            if (contentType != null) {
                builder.contentType(contentType);
            } else {
                builder.contentType(MediaType.APPLICATION_JSON);
            }
            return builder.body(response.getBody());
        } catch (Exception e) {
            return fail(HttpStatus.BAD_GATEWAY, BizStatus.ERROR, e.getMessage());
        }
    }

    /**
     * 构建失败响应
     */
    private ResponseEntity<ApiResponse<Void>> fail(HttpStatus status, String message) {
        BizStatus bizStatus = status == HttpStatus.UNAUTHORIZED ? BizStatus.UNAUTHORIZED : BizStatus.BAD_REQUEST;
        return fail(status, bizStatus, message);
    }

    /**
     * 构建失败响应
     */
    private ResponseEntity<ApiResponse<Void>> fail(HttpStatus status, BizStatus bizStatus, String message) {
        return ResponseEntity.status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.build(bizStatus, message, null));
    }
}
