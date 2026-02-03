package com.yan.xiaoyanqtserve.filter;

import com.yan.xiaoyanqtserve.security.AdminUserDetailsService;
import com.yan.xiaoyanqtserve.util.JwtUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static com.yan.xiaoyanqtserve.common.Constants.HEADER_AUTH_NAME;
import static com.yan.xiaoyanqtserve.common.Constants.HEADER_PREFIX;

@Slf4j
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Resource
    private JwtUtil jwtUtil;

    @Resource
    private AdminUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        final String requestURI = request.getRequestURI();

        // 如果是 /admin/** 路径，才尝试处理 JWT
        if (requestURI.startsWith("/admin/")) {
            final String header = request.getHeader(HEADER_AUTH_NAME);
            String username = null;
            String jwtToken = null;

            if (header != null && header.startsWith(HEADER_PREFIX)) {
                jwtToken = header.substring(HEADER_PREFIX.length());
                try {
                    username = jwtUtil.extractUsername(jwtToken);
                } catch (Exception e) {
                    log.error("请求地址：{}，Token 无效：{}", request.getRequestURI(), e.getMessage());
                }
            }

            // 如果从 Token 中提取到了用户名，并且 SecurityContext 中还没有认证信息
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // 从数据库中获取用户详情
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

                // 验证 Token 是否有效（用户名匹配且未过期）
                if (jwtUtil.validateToken(jwtToken)) {
                    // 创建 Authentication 对象
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails,
                            null,
                            userDetails.getAuthorities());
                    // 将认证信息放入 Security Context
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        }

        // 继续执行过滤器链
        filterChain.doFilter(request, response);
    }

}