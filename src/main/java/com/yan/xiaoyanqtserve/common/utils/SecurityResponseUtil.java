package com.yan.xiaoyanqtserve.common.utils;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.yan.xiaoyanqtserve.common.Constants;
import com.yan.xiaoyanqtserve.common.enums.BizStatus;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * Spring Security 响应工具类
 */
@Component
public class SecurityResponseUtil {

    private ObjectMapper objectMapper;

    @Resource
    public void setObjectMapper(ObjectMapper objectMapper) {
        // 创建副本，避免影响全局 ObjectMapper
        this.objectMapper = objectMapper.copy();

        // 配置日期格式
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        this.objectMapper.setTimeZone(TimeZone.getTimeZone("GMT+8"));
    }

    /**
     * 构建错误响应
     * 请求成功(200)，但是业务失败
     * @param response 响应对象
     * @param bizStatus   业务状态码
     * @param message  错误信息
     * @throws IOException IO异常
     */
    public void writeError(HttpServletResponse response, BizStatus bizStatus, String message) throws IOException {
        writeError(response, 200, bizStatus, message, null);
    }

    /**
     * 构建错误响应
     * @param response 响应对象
     * @param httpStatus HTTP状态码
     * @param bizStatus   业务状态码
     * @param message  错误信息
     * @param data     数据
     * @throws IOException IO异常
     */
    public <T> void writeError(HttpServletResponse response,
                               Integer httpStatus,
                               BizStatus bizStatus,
                               String message,
                               T data) throws IOException {
        response.setStatus(httpStatus);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(Constants.DEFAULT_CHARSET.name());

        ApiResponse<T> api = ApiResponse.build(bizStatus, message, data);
        String json = objectMapper.writeValueAsString(api);

        response.getWriter().write(json);
        response.getWriter().flush();
    }

}
