package com.yan.xiaoyanqtserve.util;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.yan.xiaoyanqtserve.common.Constants;
import com.yan.xiaoyanqtserve.common.enums.BizStatus;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Spring Security 响应工具类
 */
@Component
public class SecurityResponseUtil {
    @Resource
    private ObjectMapper objectMapper;

    /**
     * 统一写错误响应
     * 请求成功，但是业务失败
     * @param response 响应对象
     * @param status   状态码
     * @param message  错误信息
     * @throws IOException IO异常
     */
    public void writeError(HttpServletResponse response, BizStatus status, String message) throws IOException {
        response.setStatus(200);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(Constants.DEFAULT_CHARSET.name());

        ApiResponse<?> api = ApiResponse.build(status, message, null);
        String json = objectMapper.writeValueAsString(api);

        response.getWriter().write(json);
    }

}
