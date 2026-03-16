package com.yan.xiaoyanqtserve.interceptor;

import com.yan.xiaoyanqtserve.common.enums.RequestType;
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
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.time.LocalDateTime;
import java.util.UUID;

import static com.yan.xiaoyanqtserve.common.utils.NetUtil.getClientIpAddress;

@Component
@Slf4j
public class RequestLoggingInterceptor implements HandlerInterceptor {
    @Resource
    private RequestLogService requestLogService;
    @Resource
    private SecurityContextUtil securityContextUtil;

    public static final String REQUEST_ID_ATTRIBUTE = "REQUEST_ID";
    public static final String START_TIME_ATTRIBUTE = "START_TIME";
    public static final String LOG_ENTITY_ATTRIBUTE = "LOG_ENTITY";

    @Override
    public boolean preHandle(HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Object handler) {
        // 生成一个唯一的请求ID，用于链路追踪
        String requestId = UUID.randomUUID().toString();
        request.setAttribute(REQUEST_ID_ATTRIBUTE, requestId);

        // 记录请求开始时间
        long startTime = System.currentTimeMillis();
        request.setAttribute(START_TIME_ATTRIBUTE, startTime);

        RequestLog logEntry = new RequestLog();
        logEntry.setRequestId(requestId);
        logEntry.setRequester(getCurrentUserInfo(request));
        logEntry.setClientIp(getClientIpAddress(request));
        logEntry.setRequestType(determineRequestType(request));
        logEntry.setRequestTime(LocalDateTime.now());
        request.setAttribute(LOG_ENTITY_ATTRIBUTE, logEntry);

        return true; // 继续执行后续的处理器
    }


    @Override
    public void afterCompletion(HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Object handler, Exception ex) {
        Long startTime = (Long) request.getAttribute(START_TIME_ATTRIBUTE);
        RequestLog logEntry = (RequestLog) request.getAttribute(LOG_ENTITY_ATTRIBUTE);

        if (startTime != null && logEntry != null) {
            long duration = System.currentTimeMillis() - startTime;
            logEntry.setRequestDuration(duration);
            logEntry.setHttpCode(response.getStatus());

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
        if(securityContextUtil.isAdmin()){
            AdminUserDetails adminUser = securityContextUtil.getAdminUser();
            if(adminUser != null) {
                return adminUser.getUsername();
            }else{
                return "用户获取失败";
            }
        }
        if(securityContextUtil.isApiKeyUser()){
            KeyInfo apiKeyUser = securityContextUtil.getApiKeyUser();
            if(apiKeyUser != null) {
                return apiKeyUser.getKeyValue();
            }else{
                return "卡密获取失败";
            }
        }
        return "匿名用户";
    }
}