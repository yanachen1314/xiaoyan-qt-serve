package com.yan.xiaoyanqtserve.controller.api;

import com.yan.xiaoyanqtserve.common.Constants;
import com.yan.xiaoyanqtserve.common.dto.AigcDealDto;
import com.yan.xiaoyanqtserve.common.enums.BizStatus;
import com.yan.xiaoyanqtserve.common.utils.SecurityContextUtil;
import com.yan.xiaoyanqtserve.entity.KeyInfo;
import com.yan.xiaoyanqtserve.security.AdminUserDetails;
import com.yan.xiaoyanqtserve.service.RequestLogService;
import com.yan.xiaoyanqtserve.common.utils.ApiResponse;
import com.yan.xiaoyanqtserve.service.impl.KeyInfoManagerService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
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
@Slf4j
@Validated
@RestController
@RequestMapping("/api")
public class ApiKeyController {

    @Resource
    private SecurityContextUtil securityContextUtil;

    /**
     * AI处理接口
     * @param aigcDealDto AI处理参数
     * @return 处理结果
     */
    @PostMapping("/deal/text")
    public ApiResponse<?> aiDeal(@Valid @RequestBody AigcDealDto aigcDealDto) {
        KeyInfo apiKeyUser = securityContextUtil.getApiKeyUser();
        UserDetails adminUser = securityContextUtil.getAdminUser();
        log.info("【请求用户】APIKEY：{} ， ADMIN：{}", apiKeyUser, adminUser);
        log.info("【身份】APIKEY：{} ， ADMIN：{}",securityContextUtil.isApiKeyUser(),securityContextUtil.isAdmin());
        log.info("请求内容: {}", aigcDealDto.getOriginalText());
        // 返回处理结果
        return ApiResponse.success("AI 处理成功", Map.of("processedContent", 123));
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



