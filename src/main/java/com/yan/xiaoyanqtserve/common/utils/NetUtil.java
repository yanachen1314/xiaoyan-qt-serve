package com.yan.xiaoyanqtserve.common.utils;

import com.yan.xiaoyanqtserve.common.Constants;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

import static com.yan.xiaoyanqtserve.common.Constants.HEADER_PREFIX;

/**
 * 网络工具类
 * @author yanchen
 * @since 2026/3/15 19:46
 */
public class NetUtil {

    /**
     * 获取客户端IP地址
     * @param request HTTP请求对象
     * @return 客户端IP地址
     */
    public static String getClientIpAddress(HttpServletRequest request) {
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    /**
     * 获取用户代理信息
     * @param request HTTP请求对象
     * @return 用户代理信息
     */
    @Nullable
    public static String getUserAgent(HttpServletRequest request) {
        return request.getHeader("User-Agent");
    }


    /**
     * 获取客户端设备ID/设备指纹
     * @param request HTTP请求对象
     * @return 客户端设备ID/设备指纹
     */
    @Nullable
    public static String getClientDeviceId(HttpServletRequest request) {
        return request.getHeader(Constants.HEADER_DEVICE_ID);
    }

    /**
     * 获取客户端类型
     * @param request HTTP请求对象
     * @return 客户端类型，{@link com.yan.xiaoyanqtserve.common.enums.ClientType}
     */
    @Nullable
    public static String getClientDeviceType(HttpServletRequest request) {
        return request.getHeader(Constants.HEADER_DEVICE_TYPE);
    }

    /**
     * 获取客户端的版本号
     * @param request HTTP请求对象
     * @return 客户端（win）的版本号，例如 1.0.0 / V1.0.0
     */
    @Nullable
    public static String getClientVersion(HttpServletRequest request) {
        return request.getHeader(Constants.HEADER_CLIENT_VERSION_NAME);
    }

    /**
     * 从请求头中提取 API 密钥
     * @param request   HTTP 请求对象
     * @return API 密钥，如果请求头中没有携带 API 密钥则返回 null
     */
    @Nullable
    public static String extractApiKeyFromHeader(HttpServletRequest request) {
        String header = request.getHeader(Constants.HEADER_KEY_NAME);
        if (StringUtils.hasText(header) && header.startsWith(HEADER_PREFIX)) {
            // 返回 header 值
            return header.substring(HEADER_PREFIX.length()).trim();
        }
        return null;
    }

    /**
     * 获取请求时的验证码校验参数
     * @param request HTTP请求对象
     * @return 验证码校验参数
     */
    @Nullable
    public static String getCaptchaVerifyParams(HttpServletRequest request) {
        return request.getHeader(Constants.HEADER_CAPTCHA_VERIFY_PARAMS);
    }

    /**
     * 获取请求头中的JWT授权信息
     * @param request HTTP请求对象
     * @return 授权信息
     */
    @Nullable
    public static String getAuthHeader(HttpServletRequest request) {
        return request.getHeader(Constants.HEADER_AUTH_NAME);
    }
}
