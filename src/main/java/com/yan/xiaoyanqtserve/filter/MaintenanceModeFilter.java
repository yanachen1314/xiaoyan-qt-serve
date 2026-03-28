package com.yan.xiaoyanqtserve.filter;

import com.yan.xiaoyanqtserve.common.dto.MaintenancePeriod;
import com.yan.xiaoyanqtserve.common.enums.BizStatus;
import com.yan.xiaoyanqtserve.common.utils.SecurityResponseUtil;
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
@Order(Integer.MIN_VALUE) // 必须最高优先级！
@Slf4j
public class MaintenanceModeFilter implements Filter {

    private final MaintenanceService maintenanceService;
    private final SecurityResponseUtil securityResponseUtil;

    public MaintenanceModeFilter(MaintenanceService maintenanceService, SecurityResponseUtil securityResponseUtil) {
        this.maintenanceService = maintenanceService;
        this.securityResponseUtil = securityResponseUtil;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws ServletException, IOException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // 1. 检查是否在维护期
        if (maintenanceService.isSystemInMaintenance()) {
            // 2. 白名单放行（如健康检查、静态资源）
            if (isAllowedPath(httpRequest.getRequestURI())) {
                chain.doFilter(request, response);
                return;
            }

            // 3. 返回维护提示
            MaintenancePeriod maintenancePeriod = maintenanceService.getMaintenancePeriod();
            securityResponseUtil.writeError(httpResponse,
                    HttpStatus.SERVICE_UNAVAILABLE.value(),
                    BizStatus.SERVICE_UNAVAILABLE,
                    maintenancePeriod.getMessage(),
                    maintenancePeriod);
            return;
        }

        // 正常流程
        chain.doFilter(request, response);
    }

    // 白名单路径（按需扩展）
    private boolean isAllowedPath(String uri) {
        return uri.startsWith("/actuator/**") ||
               uri.startsWith("/swagger-ui") ||
               uri.startsWith("/v3/api-docs") ||
               uri.equals("/favicon.ico") ||
               uri.startsWith("/static/") ||
               uri.startsWith("/resources/");
    }
}