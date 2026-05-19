package com.yan.xiaoyanqtserve.config.frame;

import com.yan.xiaoyanqtserve.common.enums.BizStatus;
import com.yan.xiaoyanqtserve.common.enums.Role;
import com.yan.xiaoyanqtserve.config.custome.CorsConfigProperties;
import com.yan.xiaoyanqtserve.filter.ApiKeyAuthFilter;
import com.yan.xiaoyanqtserve.filter.JwtAuthFilter;
import com.yan.xiaoyanqtserve.common.utils.SecurityResponseUtil;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.savedrequest.NullRequestCache;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;


/**
 * Spring Security 配置
 */
@Slf4j
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
// 启用方法级权限控制, @PreAuthorize 等注解
@EnableMethodSecurity()
public class SecurityConfig {

    @Resource
    private SecurityResponseUtil responseUtil;
    @Resource
    private ApiKeyAuthFilter apiKeyAuthFilter;
    @Resource
    private JwtAuthFilter jwtAuthFilter;
    @Resource
    private CorsConfigProperties corsConfigProperties;


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * 创建 CORS 配置源
     * 从 YAML 配置文件中读取 CORS 相关配置
     *
     * @return CorsConfigurationSource CORS 配置源
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        log.info("============ CORS 配置 ===========");
        log.info("{}", corsConfigProperties);
        CorsConfiguration cfg = new CorsConfiguration();
        // 允许访问API的地址
        cfg.setAllowedOriginPatterns(corsConfigProperties.getAllowedOriginPatterns());
        // 允许的请求方法
        cfg.setAllowedMethods(corsConfigProperties.getAllowedMethods());
        // 允许的请求头
        cfg.setAllowedHeaders(corsConfigProperties.getAllowedHeaders());
        // 暴露响应头
        cfg.setExposedHeaders(corsConfigProperties.getExposedHeaders());
        // 预检请求缓存时间（秒）
        cfg.setMaxAge(corsConfigProperties.getMaxAge());
        // 是否允许携带 Cookie（使用 allowedOriginPatterns 时可设为 true）
        cfg.setAllowCredentials(corsConfigProperties.getAllowCredentials());
        // 注册对所有路径都生效
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }


    /**
     * 公共安全配置
     *
     * @param http HttpSecurity
     * @throws Exception 抛出
     */
    public void baseConfig(HttpSecurity http) throws Exception {
        http
                // 启用 WebMvc Cors 配置
                .cors(cors -> {
                    cors.configurationSource(corsConfigurationSource());
                })

                // 放行OPTIONS预检请求
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll();
                })

                // 禁用 表单登录/登出、session管理、csrf防护、匿名身份等默认配置，如果不disable。会默认创建默认filter
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .anonymous(Customizer.withDefaults())
                // 请求缓存用于重定向，前后端分离项目无需重定向，请求缓存也用不上
                .requestCache(cache -> cache.requestCache(new NullRequestCache()))
                // 禁用 session或者设置为无状态
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 异常处理：无权限
                .exceptionHandling(exception -> exception
                        .accessDeniedHandler((req, res, authEx) ->
                                responseUtil.writeError(res, BizStatus.FORBIDDEN, "无权限访问")));

    }

    /**
     *  公共接口链（最高优先级）
     * @param http HttpSecurity
     * @return SecurityFilterChain
     * @throws Exception 抛出
     */
    @Bean
    @Order(1)
    public SecurityFilterChain publicChain(HttpSecurity http) throws Exception {
        baseConfig(http);
        http
                .securityMatcher("/api/public/**")
                .authorizeHttpRequests(auth ->
                        auth.anyRequest().permitAll());
        return http.build();
    }

    /**
     * API KEY 安全链
     * @param http HttpSecurity
     * @return SecurityFilterChain
     * @throws Exception 抛出
     */
    @Bean
    @Order(2)
    public SecurityFilterChain apiChain(HttpSecurity http) throws Exception {
        baseConfig(http);
        http
                .securityMatcher("/api/key/**")
                // 异常处理：未认证
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((req, res, ex) ->
                                        responseUtil.writeError(
                                                res,
                                                BizStatus.UNAUTHORIZED,
                                                "API Key 无效"))
                )
                .authorizeHttpRequests(auth ->
                        auth.anyRequest()
                                .hasRole(Role.KEY_USER.name()))
                .addFilterBefore(apiKeyAuthFilter,UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    /**
     * Admin JWT 安全链
     * @param http HttpSecurity
     * @return SecurityFilterChain
     * @throws Exception 抛出
     */
    @Bean
    @Order(3)
    public SecurityFilterChain adminChain(HttpSecurity http) throws Exception {
        baseConfig(http);
        http
                .securityMatcher("/api/admin/**")
                // 异常处理：未认证
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((req, res, ex) ->
                                        responseUtil.writeError(res, BizStatus.UNAUTHORIZED, "登录已失效")))

                .authorizeHttpRequests(auth ->
                        auth.anyRequest()
                                .hasRole(Role.ADMIN.name()))
                .addFilterBefore(jwtAuthFilter,UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
