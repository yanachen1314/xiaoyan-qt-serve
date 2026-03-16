package com.yan.xiaoyanqtserve.interceptor;

import com.yan.xiaoyanqtserve.service.RequestLogService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.UUID;

@Component
@Slf4j
public class RequestLoggingInterceptor implements HandlerInterceptor {
    @Resource
    private RequestLogService requestLogService;

    public static final String REQUEST_ID_ATTRIBUTE = "REQUEST_ID";
    public static final String START_TIME_ATTRIBUTE = "START_TIME";

    @Override
    public boolean preHandle(HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Object handler) {
        // 生成一个唯一的请求ID，用于链路追踪
        String requestId = UUID.randomUUID().toString();
        request.setAttribute(REQUEST_ID_ATTRIBUTE, requestId);

        // 记录请求开始时间
        long startTime = System.currentTimeMillis();
        request.setAttribute(START_TIME_ATTRIBUTE, startTime);

        // 获取当前用户信息（来自SecurityContext或API Key）
        String userInfo = getCurrentUserInfo(request);

        log.info("[{}] - {} - {} - {} - {}",
                requestId,
                request.getMethod(),
                request.getRequestURI(),
                userInfo != null ? userInfo : "Anonymous",
                request.getRemoteAddr()
        );

        return true; // 继续执行后续的处理器
    }

    @Override
    public void postHandle(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Object handler, ModelAndView modelAndView) throws Exception {
        // 可在此处添加请求处理完成后的逻辑（如响应头等），但统计耗时放在afterCompletion更准确
    }

    @Override
    public void afterCompletion(HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Object handler, Exception ex) throws Exception {
        // 获取请求ID和开始时间
        String requestId = (String) request.getAttribute(REQUEST_ID_ATTRIBUTE);
        Long startTime = (Long) request.getAttribute(START_TIME_ATTRIBUTE);

        if (startTime != null) {
            long duration = System.currentTimeMillis() - startTime;

            log.info("[{}] - {} - Status: {} - Duration: {}ms",
                    requestId,
                    request.getRequestURI(),
                    response.getStatus(),
                    duration
            );
        }
    }

    /**
     * 获取当前用户信息，例如从SecurityContext或解析API Key
     * 这里需要根据你的具体认证方式来实现。
     * 您之前提到想封装Spring Security上下文，可以在这里调用。
     * 如果是API Key，则可能需要从请求头或参数中解析。
     */
    private String getCurrentUserInfo(HttpServletRequest request) {
        // --- 示例：从Spring Security上下文获取 ---
        // Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
        //     return authentication.getName(); // 返回用户名
        // }

        // --- 示例：从自定义Header获取API Key用户信息 ---
        // String apiKey = request.getHeader("X-API-Key"); // 假设API Key放在这个Header里
        // if (apiKey != null) {
        //     // 根据apiKey查询数据库或缓存，获取用户标识
        //     // KeyInfo keyInfo = keyInfoManagerService.findByKey(apiKey);
        //     // return keyInfo != null ? "ApiKey:" + keyInfo.getId() : "InvalidApiKey";
        // }

        // --- 结合您的系统 ---
        // 您可能需要结合Spring Security和API Key两种情况。
        // 例如，先看SecurityContext是否有管理员，再看是否有API Key。
        // 这部分逻辑可以根据您的认证流程进行定制。
        // 为了示例，这里返回null，实际应用中应填充此逻辑。
        return null;
    }
}