package com.yan.xiaoyanqtserve.filter;

import com.yan.xiaoyanqtserve.entity.KeyInfo;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

import static com.yan.xiaoyanqtserve.common.Constants.HEADER_NAME;

@Component
public class ApiKeyAuthFilter extends OncePerRequestFilter {


    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        // 1. 从请求头中提取 API Key
        String apiKey = extractApiKeyFromHeader(request);

        // 2. 如果没有 API Key，继续执行后续过滤器链 (让其他安全规则处理，比如 /admin/** 会被拦住)
        if (!StringUtils.hasText(apiKey)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. 使用你的服务验证 API Key，并获取相关密钥信息
        // 注意：这里调用 requestProcessingService.processRequest(keyValue) 会直接扣减次数！
        // 如果只是想验证而不扣次，需要在 service 层单独加一个验证方法，或者将扣次逻辑移到 controller。
        // 假设我们有一个只验证不扣次的方法 validateKey
        Optional<KeyInfo> keyInfoOpt = requestProcessingService.validateKey(apiKey); // 假设此方法存在

        if (keyInfoOpt.isPresent()) {
            // 4. 如果 API Key 有效，创建一个 Authentication 对象
            // Principal 可以是 KeyInfo 对象或其 ID，Credentials 通常是 null (因为不是密码认证)
            // Authorities 可以根据业务需要赋予，例如 ROLE_API_USER
            KeyInfo keyInfo = keyInfoOpt.get();
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(keyInfo.getKeyValue(), null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_API_USER")));

            // 5. 将认证信息放入 Security Context
            SecurityContextHolder.getContext().setAuthentication(authToken);
        } else {
            // 6. 如果 API Key 无效，可以选择直接返回错误，或者继续链（由后续规则处理，但通常这里直接返回更清晰）
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 或者 SC_FORBIDDEN
            response.getWriter().write("{\"error\":\"Invalid API Key\"}");
            response.getWriter().flush();
            return; // 阻止请求继续
        }

        // 7. 继续执行过滤器链
        filterChain.doFilter(request, response);
    }

    private String extractApiKeyFromHeader(HttpServletRequest request) {
        String header = request.getHeader(HEADER_NAME);
        if (header != null && header.startsWith(" ")) { // 如果需要 Bearer 这样的前缀，可以这样处理
            return header.substring(1).trim();
        }
        return header; // 直接返回整个 header 值
    }

    // 决定此过滤器是否应用于特定请求路径
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        // 例如，不对 /actuator/health 等路径应用此过滤器
        String path = request.getRequestURI();
        return path.startsWith("/actuator/health") || path.startsWith("/css/") || path.startsWith("/js/");
    }
}