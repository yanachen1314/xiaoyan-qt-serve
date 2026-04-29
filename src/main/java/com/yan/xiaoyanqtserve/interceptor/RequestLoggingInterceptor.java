package com.yan.xiaoyanqtserve.interceptor;

import com.yan.xiaoyanqtserve.common.Constants;
import com.yan.xiaoyanqtserve.common.enums.RequestType;
import com.yan.xiaoyanqtserve.common.utils.NetUtil;
import com.yan.xiaoyanqtserve.common.utils.SecurityContextUtil;
import com.yan.xiaoyanqtserve.entity.KeyInfo;
import com.yan.xiaoyanqtserve.entity.RequestLog;
import com.yan.xiaoyanqtserve.security.AdminUserDetails;
import com.yan.xiaoyanqtserve.service.RequestLogService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.time.LocalDateTime;
import java.util.UUID;


@Component
@Slf4j
public class RequestLoggingInterceptor implements HandlerInterceptor {
    @Resource
    private RequestLogService requestLogService;
    @Resource
    private SecurityContextUtil securityContextUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Object handler) {
        // 生成一个唯一的请求ID，用于链路追踪
        String requestId = UUID.randomUUID().toString();
        request.setAttribute(Constants.ATTRIBUTE_REQUEST_ID, requestId);

        // 记录请求开始时间
        long startTime = System.currentTimeMillis();
        request.setAttribute(Constants.ATTRIBUTE_START_TIME, startTime);

        RequestLog logEntry = new RequestLog();
        logEntry.setRequestId(requestId);
        logEntry.setRequester(getCurrentUserInfo(request));
        logEntry.setRequestUrl(request.getRequestURI());
        logEntry.setClientIp(NetUtil.getClientIpAddress(request));
        logEntry.setRequestType(determineRequestType(request));
        logEntry.setRequestTime(LocalDateTime.now());
        request.setAttribute(Constants.ATTRIBUTE_LOG_ENTITY, logEntry);

        return true; // 继续执行后续的处理器
    }


    @Override
    public void afterCompletion(HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Object handler, Exception ex) {
        Long startTime = (Long) request.getAttribute(Constants.ATTRIBUTE_START_TIME);
        RequestLog logEntry = (RequestLog) request.getAttribute(Constants.ATTRIBUTE_LOG_ENTITY);

        if (startTime != null && logEntry != null) {
            long duration = System.currentTimeMillis() - startTime;
            logEntry.setRequestDuration(duration);
            logEntry.setHttpCode(response.getStatus());

            // --- 从请求属性中获取业务信息 ---
            Integer bizCode = (Integer) request.getAttribute(Constants.ATTRIBUTE_BIZ_CODE);
            String responseMessage = (String) request.getAttribute(Constants.ATTRIBUTE_RESPONSE_MESSAGE);

            // 设置到日志实体中
            logEntry.setBizCode(bizCode);
            logEntry.setResponseMessage(responseMessage);

            // 调用异步保存方法
            requestLogService.saveLogAsync(logEntry);
        }
    }

    private String determineRequestType(HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        return RequestType.getByUrl(requestURI).getCode();
    }

    /**
     * 获取当前用户信息，此时SecurityContext已认证，可以安全获取用户信息
     * 但是公开接口无法获取用户信息
     */
    private String getCurrentUserInfo(HttpServletRequest request) {
        // 已认证的管理员用户
        if (securityContextUtil.isAdmin()) {
            AdminUserDetails adminUser = securityContextUtil.getAdminUser();
            if (adminUser != null) {
                return adminUser.getUsername();
            } else {
                return "用户获取失败";
            }
        }
        // 已认证的API密钥用户
        if (securityContextUtil.isApiKeyUser()) {
            KeyInfo apiKeyUser = securityContextUtil.getApiKeyUser();
            if (apiKeyUser != null) {
                return apiKeyUser.getKeyValue();
            } else {
                return "卡密获取失败";
            }
        }
        // 匿名用户（公开接口）
        String apiKey = NetUtil.extractApiKeyFromHeader(request);
        if (apiKey != null) return  "匿名用户："+apiKey;
        return "匿名用户";
    }
}