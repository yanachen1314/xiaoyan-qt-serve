package com.yan.xiaoyanqtserve.controller.publicApi;

import com.yan.xiaoyanqtserve.common.Constants;
import com.yan.xiaoyanqtserve.common.dto.tool.ToolVerifyRequest;
import com.yan.xiaoyanqtserve.common.dto.tool.ToolVerifyResponse;
import com.yan.xiaoyanqtserve.common.enums.BizStatus;
import com.yan.xiaoyanqtserve.common.exception.BizException;
import com.yan.xiaoyanqtserve.common.utils.ApiResponse;
import com.yan.xiaoyanqtserve.service.impl.tool.ToolAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

/**
 * 公开工具校验接口，用于将微信公众号验证码换成一次性工具 JWT。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/public/tool-auth")
public class ToolAuthController {
    private final ToolAuthService toolAuthService;

    /**
     * 校验微信公众号验证码并返回工具令牌。
     *
     * @param deviceId 浏览器指纹
     * @param request 校验请求
     * @return 工具 JWT
     */
    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<ToolVerifyResponse>> verify(
            @RequestHeader(value = Constants.HEADER_DEVICE_ID, required = false) String deviceId,
            @Valid @RequestBody ToolVerifyRequest request
    ) {
        if (!StringUtils.hasText(deviceId)) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.build(BizStatus.BAD_REQUEST, "缺少浏览器指纹", null));
        }

        try {
            ToolVerifyResponse response = toolAuthService.verifyAndIssueToken(
                    request.toolCode(),
                    request.code(),
                    deviceId
            );
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (BizException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.build(e.getCode(), e.getMessage(), null));
        }
    }
}
