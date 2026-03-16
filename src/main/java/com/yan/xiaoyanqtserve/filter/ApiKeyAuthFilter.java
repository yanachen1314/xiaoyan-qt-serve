package com.yan.xiaoyanqtserve.filter;

import com.yan.xiaoyanqtserve.common.dto.key.ValidationResult;
import com.yan.xiaoyanqtserve.common.enums.BizStatus;
import com.yan.xiaoyanqtserve.common.enums.Role;
import com.yan.xiaoyanqtserve.common.utils.RedisUtil;
import com.yan.xiaoyanqtserve.entity.KeyInfo;
import com.yan.xiaoyanqtserve.service.impl.KeyInfoManagerService;
import com.yan.xiaoyanqtserve.common.utils.SecurityResponseUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import static com.yan.xiaoyanqtserve.common.utils.MainUtil.extractApiKeyFromHeader;

@Slf4j
@Component
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    @Resource
    private SecurityResponseUtil securityResponseUtil;
    @Resource
    private KeyInfoManagerService keyInfoManagerService;

    @Resource
    private RedisUtil redisUtil;

    // 密钥使用时的校验缓存前缀
    private static final String USAGE_VALID_KEY = "valid:usage:";

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        if (!request.getRequestURI().startsWith("/api/key")) {
            filterChain.doFilter(request, response);
            return;
        }
        // 1. 从请求头中提取 API Key
        String apiKey = extractApiKeyFromHeader(request);

        // 2. 如果没有 API Key，继续执行后续过滤器链
        if (!StringUtils.hasText(apiKey)) {
            // filterChain.doFilter(request, response);
            securityResponseUtil.writeError(response, BizStatus.UNAUTHORIZED, "缺少 API Key");
            return;
        }

        // 3. 使用你的服务验证 API Key
        ValidationResult validationResult = null;
        String cacheKey = USAGE_VALID_KEY + apiKey;
        if (redisUtil.hasKey(cacheKey)) {
            // 从缓存中获取验证结果
            log.info("API KEY({})使用了缓存校验结果", apiKey);
            validationResult = (ValidationResult) redisUtil.get(cacheKey);
        } else {
            // 如果缓存中不存在，调用服务进行验证
            validationResult = keyInfoManagerService.filterKeyValid(apiKey);
            // 缓存校验结果，有效期 1 分钟
            redisUtil.set(cacheKey, validationResult,1, TimeUnit.MINUTES);
        }

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
            securityResponseUtil.writeError(response, validationResult.getErrorCode(),
                    validationResult.getErrorMessage() == null ? validationResult.getErrorCode().msg() : validationResult.getErrorMessage());
            return; // 阻止请求继续
        }

        // 7. 继续执行过滤器链
        filterChain.doFilter(request, response);
    }


}