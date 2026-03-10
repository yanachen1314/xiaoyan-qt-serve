package com.yan.xiaoyanqtserve.controller.api;

import com.yan.xiaoyanqtserve.common.Constants;
import com.yan.xiaoyanqtserve.common.enums.BizStatus;
import com.yan.xiaoyanqtserve.service.RequestLogService;
import com.yan.xiaoyanqtserve.common.utils.ApiResponse;
import com.yan.xiaoyanqtserve.service.impl.KeyInfoManagerService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * API密钥处理接口
 * @author yanchen
 * @since 2026/2/3 16:59
 */
@RestController
@RequestMapping("/api")
public class ApiKeyController {

    @Resource
    private KeyInfoManagerService keyInfoManagerService;

    @Resource
    private RequestLogService requestLogService;

    /**
     * 密钥激活接口
     * @param request HttpServletRequest
     * @return 激活结果
     */
    @PostMapping("/active")
    public ApiResponse<?> activateKey(HttpServletRequest request) {
        // 从请求头中获取 API Key
        String apiKey = extractApiKeyFromHeader(request);

        // 验证 API Key 是否存在
        if (!StringUtils.hasText(apiKey)) {
            // 记录请求日志
            requestLogService.recordRequestLog(null, getClientIp(request),
                    HttpStatus.BAD_REQUEST.value(), "缺少 API Key");

            return ApiResponse.fail(BizStatus.BAD_REQUEST, "缺少 API Key");
        }

        // 调用 KeyInfoService 的 activateKey 方法激活密钥
        var optionalKeyInfo = keyInfoManagerService.activateKey(apiKey);

        if (optionalKeyInfo.isPresent()) {
            // 记录请求日志
            requestLogService.recordRequestLog(apiKey, getClientIp(request),
                    HttpStatus.OK.value(), "密钥激活成功");

            // 激活成功
            return ApiResponse.success("密钥激活成功",
                    Map.of("activatedTime", optionalKeyInfo.get().getActivationTime())
            );
        } else {
            // 记录请求日志
            requestLogService.recordRequestLog(apiKey, getClientIp(request),
                    HttpStatus.BAD_REQUEST.value(), "密钥激活失败，密钥不存在或状态不正确");

            // 激活失败
            return ApiResponse.fail(BizStatus.BAD_REQUEST, "密钥激活失败，密钥不存在或状态不正确");
        }
    }

    /**
     * AI处理接口
     * @param request HttpServletRequest
     * @param requestBody 请求体
     * @return 处理结果
     */
    @PostMapping("/ai/deal")
    public ApiResponse<?> aiDeal(HttpServletRequest request, @RequestBody Map<String, Object> requestBody) {
        // 从请求头中获取 API Key
        String apiKey = extractApiKeyFromHeader(request);

        // 验证 API Key 是否存在
        if (!StringUtils.hasText(apiKey)) {
            // 记录请求日志
            requestLogService.recordRequestLog(null, getClientIp(request),
                    HttpStatus.BAD_REQUEST.value(), "缺少 API Key");

            return ApiResponse.fail(BizStatus.BAD_REQUEST, "缺少 API Key");
        }

        // 验证请求参数
        if (!requestBody.containsKey("content")) {
            // 记录请求日志
            requestLogService.recordRequestLog(apiKey, getClientIp(request),
                    HttpStatus.BAD_REQUEST.value(), "缺少必要的请求参数 content");

            return ApiResponse.fail(BizStatus.BAD_REQUEST, "缺少必要的请求参数 content");
        }

        String content = requestBody.get("content").toString();
        if (!StringUtils.hasText(content)) {
            // 记录请求日志
            requestLogService.recordRequestLog(apiKey, getClientIp(request),
                    HttpStatus.BAD_REQUEST.value(), "content 参数不能为空");

            return ApiResponse.fail(BizStatus.BAD_REQUEST, "content 参数不能为空");
        }

        // 调用 KeyInfoService 的 decrementKeyUses 方法扣减使用次数
        boolean decremented = keyInfoManagerService.decrementKeyUses(apiKey);
        if (!decremented) {
            // 记录请求日志
            requestLogService.recordRequestLog(apiKey, getClientIp(request),
                    HttpStatus.BAD_REQUEST.value(), "密钥使用次数扣减失败，密钥不存在或状态不正确");

            return ApiResponse.fail(BizStatus.BAD_REQUEST, "密钥使用次数扣减失败，密钥不存在或状态不正确");
        }

        // 执行核心业务逻辑（示例：AIGC 降重）
        String processedContent = processAiContent(content);

        // 记录请求日志
        requestLogService.recordRequestLog(apiKey, getClientIp(request),
                HttpStatus.OK.value(), "AI 处理成功");

        // 返回处理结果
        return ApiResponse.success("AI 处理成功", Map.of("processedContent", processedContent));
    }

    /**
     * 从请求头中提取 API Key
     * @param request HttpServletRequest
     * @return API Key
     */
    private String extractApiKeyFromHeader(HttpServletRequest request) {
        String header = request.getHeader(Constants.HEADER_KEY_NAME);
        if (StringUtils.hasText(header) && header.startsWith(Constants.HEADER_PREFIX)) {
            // 返回 header 值
            return header.substring(Constants.HEADER_PREFIX.length()).trim();
        }
        return null;
    }

    /**
     * 执行 AI 核心业务逻辑（示例：AIGC 降重）
     * @param content 原始内容
     * @return 处理后的内容
     */
    private String processAiContent(String content) {
        // 这里是示例实现，实际项目中应该调用真实的 AI 模型
        // 示例：简单的文本处理，模拟 AIGC 降重功能
        return "处理后的内容：" + content.replace(" ", " ");
    }

    /**
     * 获取客户端IP地址
     * @param request HttpServletRequest
     * @return 客户端IP地址
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}



