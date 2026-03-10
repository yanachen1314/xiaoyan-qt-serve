package com.yan.xiaoyanqtserve.common.utils;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yan.xiaoyanqtserve.common.dto.PageResponse;
import com.yan.xiaoyanqtserve.common.enums.BizStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 业务统一响应实体类
 *
 * @param <T>
 */
@Data
public class ApiResponse<T> {
    // 响应时间，格式为：yyyy-MM-dd HH:mm:ss
    private String timestamp;
    // 业务状态码
    private int code;
    // 响应消息
    private String message;
    // 数据
    private T data;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private ApiResponse(String timestamp, int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = timestamp;
    }

    /**
     * 成功
     *
     * @return ApiResponse
     * @param <T> 数据类型
     */
    public static <T> ApiResponse<T> success() {
        return build(BizStatus.SUCCESS, BizStatus.SUCCESS.msg(), null);
    }

    /**
     * 成功（带数据）
     *
     * @param data 数据
     * @return ApiResponse
     * @param <T> 数据类型
     */
    public static <T> ApiResponse<T> success(T data) {
        return build(BizStatus.SUCCESS, BizStatus.SUCCESS.msg(), data);
    }

    /**
     * 成功（自定义消息+数据）
     *
     * @param message 自定义消息
     * @param data    数据
     * @return ApiResponse
     * @param <T> 数据类型
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return build(BizStatus.SUCCESS, message, data);
    }


    /**
     * 失败
     *
     * @param message 自定义错误信息
     * @return ApiResponse
     * @param <T> 数据类型
     */
    public static <T> ApiResponse<T> fail(String message) {
        return build(BizStatus.ERROR, message, null);
    }

    /**
     * 失败（自定义信息 + 自定义响应数据）
     *
     * @param message 错误信息
     * @param data    data 数据
     * @return ApiResponse
     * @param <T> 数据类型
     */
    public static <T> ApiResponse<T> fail(String message, T data) {
        return build(BizStatus.ERROR, message, data);
    }

    /**
     * 失败（自定义错误码 + 自定义消息）
     *
     * @param status  自定义错误码
     * @param message 错误信息
     * @return ApiResponse
     * @param <T> 数据类型
     */
    public static <T> ApiResponse<T> fail(BizStatus status, String message) {
        return build(status, message, null);
    }
    /**
     * 失败（自定义错误码 + 错误码默认消息）
     *
     * @param status  错误码及其消息
     * @return ApiResponse
     * @param <T> 数据类型
     */
    public static <T> ApiResponse<T> fail(BizStatus status) {
        return build(status, status.msg(), null);
    }


    /**
     * 构建响应对象
     *
     * @param status  状态码
     * @param message 消息
     * @param data    数据
     * @return ApiResponse
     * @param <T> 数据类型
     */
    public static <T> ApiResponse<T> build(BizStatus status, String message, T data) {
        return new ApiResponse<>(LocalDateTime.now().format(FORMATTER), status.code(), message, data);
    }

    /**
     * 分页响应封装（基于 MyBatis-Plus IPage 对象）
     * @param page IPage 对象
     * @return ApiResponse 封装的分页响应体
     * @param <T> 数据类型
     */
    public static <T> ApiResponse<PageResponse<T>> page(IPage<T> page) {
        PageResponse<T> pageData = new PageResponse<>(
                page.getTotal(),
                page.getPages(),
                page.getCurrent(),
                page.getSize(),
                page.getRecords()
        );
        return success(pageData);
    }

}
