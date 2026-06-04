package com.yan.xiaoyanqtserve.controller.publicApi;

import com.yan.xiaoyanqtserve.common.Constants;
import com.yan.xiaoyanqtserve.common.annotaions.RateLimiter;
import com.yan.xiaoyanqtserve.common.dto.tool.DoubaoAnalyzeRequest;
import com.yan.xiaoyanqtserve.common.enums.BizStatus;
import com.yan.xiaoyanqtserve.common.enums.RateLimiterType;
import com.yan.xiaoyanqtserve.common.exception.BizException;
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
 * 豆包工具网关接口，负责校验工具 JWT 后代理到 Flask 解析服务。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/public/tools/doubao")
public class DoubaoToolController {
    private static final String TOOL_CODE = "doubao";
    private static final String ANALYZE_RATE_LIMIT_KEY = "doubaoAnalyze";

    private final ToolAuthService toolAuthService;
    private final DoubaoToolProxyService proxyService;

    /**
     * 解析豆包图片素材。
     *
     * @param deviceId 浏览器指纹
     * @param toolToken 工具 JWT
     * @param request 解析请求
     * @return Flask 图片解析响应
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
     * 解析豆包视频素材。
     *
     * @param deviceId 浏览器指纹
     * @param toolToken 工具 JWT
     * @param request 解析请求
     * @return Flask 视频解析响应
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
        } catch (BizException e) {
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
        } catch (BizException e) {
            return fail(HttpStatus.BAD_GATEWAY, BizStatus.ERROR, e.getMessage());
        }
    }

    private ResponseEntity<ApiResponse<Void>> fail(HttpStatus status, String message) {
        BizStatus bizStatus = status == HttpStatus.UNAUTHORIZED ? BizStatus.UNAUTHORIZED : BizStatus.BAD_REQUEST;
        return fail(status, bizStatus, message);
    }

    private ResponseEntity<ApiResponse<Void>> fail(HttpStatus status, BizStatus bizStatus, String message) {
        return ResponseEntity.status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.build(bizStatus, message, null));
    }
}
