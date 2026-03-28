package com.yan.xiaoyanqtserve.filter;

import com.yan.xiaoyanqtserve.service.impl.MaintenanceService;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
@Order(1) // 必须最高优先级！
@Slf4j
public class MaintenanceModeFilter implements Filter {

    private final MaintenanceService maintenanceService;

    public MaintenanceModeFilter(MaintenanceService maintenanceService) {
        this.maintenanceService = maintenanceService;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws ServletException, IOException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // ✅ 1. 检查是否在维护期
        if (maintenanceService.isSystemInMaintenance()) {
            // ✅ 2. 白名单放行（如健康检查、静态资源）
            if (isAllowedPath(httpRequest.getRequestURI())) {
                chain.doFilter(request, response);
                return;
            }

            // 🔴 3. 返回维护提示
            httpResponse.setStatus(HttpStatus.SERVICE_UNAVAILABLE.value());
            httpResponse.setContentType("application/json;charset=UTF-8");

            String message = "系统正在维护中，请稍后重试。";
            // 可从 DB 或配置中读取更友好的提示（如 config.remark 字段）
            ConfigEntity config = maintenanceService.getMaintenancePeriod() != null ?
                    maintenanceService.getConfigEntity() : null;
            if (config != null && StringUtils.isNotBlank(config.getRemark())) {
                message = config.getRemark();
            }

            httpResponse.getWriter().write(
                JSON.toJSONString(Map.of(
                    "code", 503,
                    "message", message,
                    "timestamp", System.currentTimeMillis(),
                    "maintenanceWindow", Map.of(
                        "start", maintenanceService.getMaintenancePeriod().getStart(),
                        "end", maintenanceService.getMaintenancePeriod().getEnd()
                    )
                ))
            );
            return;
        }

        // ✅ 正常流程
        chain.doFilter(request, response);
    }

    // 白名单路径（按需扩展）
    private boolean isAllowedPath(String uri) {
        return uri.startsWith("/actuator/health") ||
               uri.startsWith("/actuator/info") ||
               uri.startsWith("/swagger-ui") ||
               uri.startsWith("/v3/api-docs") ||
               uri.equals("/favicon.ico") ||
               uri.startsWith("/static/") ||
               uri.startsWith("/resources/");
    }
}