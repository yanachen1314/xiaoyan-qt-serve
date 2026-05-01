package com.yan.xiaoyanqtserve.filter;

import com.yan.xiaoyanqtserve.common.Constants;
import com.yan.xiaoyanqtserve.common.dto.MaintenancePeriod;
import com.yan.xiaoyanqtserve.common.enums.BizStatus;
import com.yan.xiaoyanqtserve.common.utils.MainUtil;
import com.yan.xiaoyanqtserve.common.utils.NetUtil;
import com.yan.xiaoyanqtserve.common.utils.SecurityResponseUtil;
import com.yan.xiaoyanqtserve.common.vo.VersionCheckVo;
import com.yan.xiaoyanqtserve.service.SysConfigService;
import com.yan.xiaoyanqtserve.service.impl.system.ClientVersionService;
import com.yan.xiaoyanqtserve.service.impl.system.MaintenanceService;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;

@Component
@Order(Integer.MIN_VALUE) // 必须最高优先级！
@Slf4j
@RequiredArgsConstructor
public class SystemModeFilter implements Filter {

    private final ClientVersionService clientVersionService;
    private final MaintenanceService maintenanceService;
    private final SecurityResponseUtil securityResponseUtil;
    private final SysConfigService sysConfigService;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws ServletException, IOException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // 1. 白名单放行（如健康检查、静态资源），优先处理
        String requestURI = httpRequest.getRequestURI();
        if (isAllowedPath(requestURI)) {
            chain.doFilter(request, response);
            return;
        }

        // 2. 检查是否更新版本，以下两种情况中任意一种出现均不做处理，直接跳过版本检查
        // （1）客户端维护状态的心跳检测接口
        // （2）WEB 端所有接口
        if(!isMaintenanceRequest(requestURI) && !isWebRequest(httpRequest)){
            String clientVersion = NetUtil.getClientVersion(httpRequest);
            if (!StringUtils.hasText(clientVersion)) {
                securityResponseUtil.writeError(httpResponse,
                        HttpStatus.OK.value(),
                        BizStatus.CLIENT_NON_VERSION,
                        "请提供客户端版本号",
                        VersionCheckVo.builder()
                                .version(sysConfigService.getValue(Constants.VERSION_KEY))
                                .clientVersion(clientVersion)
                                .latest(false)
                                .updateRequired(false)
                                .message("请提供客户端版本号或下载最新版本")
                                .linkUrl(sysConfigService.getValue(Constants.LINK3_KEY))
                                .build());
                return ;
            }
            VersionCheckVo versionCheckVo = clientVersionService.compareVersions(clientVersion);
            if (versionCheckVo.getUpdateRequired()) {
                securityResponseUtil.writeError(httpResponse,
                        HttpStatus.OK.value(),
                        BizStatus.CLIENT_UPDATE_REQUIRED,
                        versionCheckVo.getMessage(),
                        versionCheckVo);
                return;
            }
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
    // 判断是否为维护请（心跳检测）
    private boolean isMaintenanceRequest(String requestUri) {
        return requestUri.equals("/api/public/check/maintenance");
    }

    // 判断是否为WEB请求
    private boolean isWebRequest(HttpServletRequest httpRequest){
        String deviceType = NetUtil.getClientDeviceType(httpRequest);
        String deviceId = NetUtil.getClientDeviceId(httpRequest);
        return "WEB".equalsIgnoreCase(deviceType) && StringUtils.hasText(deviceId);
    }

    // 白名单路径（按需扩展）
    private boolean isAllowedPath(String requestUri) {
        String[] allowedPatterns = {
                // 管理员登录接口
                "/api/public/auth/**",
                // 管理员运维接口
                "/api/admin/**",
                // 验证链接校验的接口
                Constants.EMAIL_VERIFY_BASE_URL
        };
        return MainUtil.isAllowedPath(allowedPatterns, requestUri);
    }
}