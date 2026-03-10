package com.yan.xiaoyanqtserve.filter;

import com.yan.xiaoyanqtserve.common.dto.key.ValidationResult;
import com.yan.xiaoyanqtserve.common.enums.BizStatus;
import com.yan.xiaoyanqtserve.common.enums.Role;
import com.yan.xiaoyanqtserve.entity.KeyInfo;
import com.yan.xiaoyanqtserve.service.impl.KeyInfoManagerService;
import com.yan.xiaoyanqtserve.common.utils.SecurityResponseUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

import static com.yan.xiaoyanqtserve.common.Constants.HEADER_PREFIX;
import static com.yan.xiaoyanqtserve.common.Constants.HEADER_KEY_NAME;

@Component
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    @Resource
    private SecurityResponseUtil securityResponseUtil;
    @Resource
    private KeyInfoManagerService keyInfoManagerService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        // 1. 从请求头中提取 API Key
        String apiKey = extractApiKeyFromHeader(request);

        // 2. 如果没有 API Key，继续执行后续过滤器链
        if (!StringUtils.hasText(apiKey)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. 使用你的服务验证 API Key
        ValidationResult validationResult = keyInfoManagerService.validateKey(apiKey);

        if (validationResult.isSuccess()) {
            // 4. 如果 API Key 有效，创建一个 Authentication 对象
            KeyInfo keyInfo = validationResult.getKeyInfo();
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(keyInfo, null,
                            Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + Role.KEY_USER.name())));

            // 5. 将认证信息放入 Security Context
            SecurityContextHolder.getContext().setAuthentication(authToken);
        } else {
            // 6. 如果 API Key 无效，返回具体的错误信息
            securityResponseUtil.writeError(response, BizStatus.UNAUTHORIZED, validationResult.getErrorMessage());
            return; // 阻止请求继续
        }

        // 7. 继续执行过滤器链
        filterChain.doFilter(request, response);
    }

    @Nullable
    private String extractApiKeyFromHeader(HttpServletRequest request) {
        String header = request.getHeader(HEADER_KEY_NAME);
        if (StringUtils.hasText(header) && header.startsWith(HEADER_PREFIX)) {
            // 返回 header 值
            return header.substring(HEADER_PREFIX.length()).trim();
        }
        return null;
    }

    // 决定此过滤器是否应用于特定请求路径
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // 例如，不对 /actuator/health 等路径应用此过滤器
        String path = request.getRequestURI();
        return path.startsWith("/actuator/health") || path.startsWith("/css/") || path.startsWith("/js/")
                || path.startsWith("/auth/");
    }
}