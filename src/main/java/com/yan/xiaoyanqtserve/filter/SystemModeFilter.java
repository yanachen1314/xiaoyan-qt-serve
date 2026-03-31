package com.yan.xiaoyanqtserve.filter;

import com.yan.xiaoyanqtserve.common.Constants;
import com.yan.xiaoyanqtserve.common.dto.MaintenancePeriod;
import com.yan.xiaoyanqtserve.common.enums.BizStatus;
import com.yan.xiaoyanqtserve.common.utils.MainUtil;
import com.yan.xiaoyanqtserve.common.utils.SecurityResponseUtil;
import com.yan.xiaoyanqtserve.common.vo.VersionCheckVo;
import com.yan.xiaoyanqtserve.service.impl.ClientVersionService;
import com.yan.xiaoyanqtserve.service.impl.MaintenanceService;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Order(Integer.MIN_VALUE) // 必须最高优先级！
@Slf4j
@RequiredArgsConstructor
public class SystemModeFilter implements Filter {

    private final ClientVersionService clientVersionService;
    private final MaintenanceService maintenanceService;
    private final SecurityResponseUtil securityResponseUtil;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws ServletException, IOException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // 1. 白名单放行（如健康检查、静态资源），优先处理
        if (isAllowedPath(httpRequest.getRequestURI())) {
            chain.doFilter(request, response);
            return;
        }

        // 2. 检查是否更新版本
        String clientVersion = httpRequest.getHeader(Constants.HEADER_CLIENT_VERSION_NAME);
        VersionCheckVo versionCheckVo = clientVersionService.compareVersions(clientVersion);
        if (versionCheckVo.getUpdateRequired()) {
            securityResponseUtil.writeError(httpResponse,
                    HttpStatus.OK.value(),
                    BizStatus.UPDATE_REQUIRED,
                    versionCheckVo.getMessage(),
                    versionCheckVo);
            return;
        }

        // 3. 检查是否在维护期
        if (maintenanceService.isSystemInMaintenance()) {
            // 3. 返回维护提示
            MaintenancePeriod maintenancePeriod = maintenanceService.getMaintenancePeriod();
            securityResponseUtil.writeError(httpResponse,
                    HttpStatus.OK.value(),
                    BizStatus.SERVICE_UNAVAILABLE,
                    maintenancePeriod.maintenanceMessage(),
                    maintenancePeriod);
            return;
        }

        // 正常流程放行
        chain.doFilter(request, response);
    }

    // 白名单路径（按需扩展）
    private boolean isAllowedPath(String requestUri) {
        String[] allowedPatterns = {
                // 管理员登录接口
                "/api/public/auth/**",
                // 管理员运维接口
                "/api/admin/**",
                "/actuator/**",
                "/favicon.ico",
                "/static/**",
                "/resources/**"
        };
        return MainUtil.isAllowedPath(allowedPatterns, requestUri);
    }
}