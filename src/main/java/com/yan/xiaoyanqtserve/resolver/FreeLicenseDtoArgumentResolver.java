package com.yan.xiaoyanqtserve.resolver;

import com.yan.xiaoyanqtserve.common.Constants;
import com.yan.xiaoyanqtserve.common.dto.FreeLicenseDto;
import com.yan.xiaoyanqtserve.common.utils.NetUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor;

/**
 * FreeLicenseDto 参数解析器
 * <p>在请求到达Controller之前自动填充设备ID、IP、User-Agent</p>
 * @author yanchen
 * @since 2026/4/23
 */
public class FreeLicenseDtoArgumentResolver implements HandlerMethodArgumentResolver {

    private final RequestResponseBodyMethodProcessor delegate;

    /**
     * 构造函数
     * @param delegate 委托给 Spring MVC 默认的 RequestBody 解析器
     */
    public FreeLicenseDtoArgumentResolver(RequestResponseBodyMethodProcessor delegate) {
        this.delegate = delegate;
    }

    /**
     * 判断是否支持该参数类型
     */
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return FreeLicenseDto.class.isAssignableFrom(parameter.getParameterType());
    }

    /**
     * 解析参数，先通过默认JSON解析器解析请求体，再自动填充设备信息
     */
    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) throws Exception {
        // 使用默认的 RequestBody 解析器解析 JSON 请求体
        FreeLicenseDto dto = (FreeLicenseDto) delegate.resolveArgument(
                parameter, mavContainer, webRequest, binderFactory);

        if (dto == null) {
            return new FreeLicenseDto();
        }

        // 获取 HttpServletRequest
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        if (request != null) {
            // 自动填充设备信息
            dto.setDeviceId(request.getHeader(Constants.HEADER_DEVICE_NAME));
            dto.setClientIp(NetUtil.getClientIpAddress(request));
            dto.setUserAgent(request.getHeader("User-Agent"));
        }

        return dto;
    }
}
