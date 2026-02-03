package com.yan.xiaoyanqtserve.util;

import com.yan.xiaoyanqtserve.common.enums.BizStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * ResponseUtil 工具类，统一封装 ResponseEntity<ApiResponse<T>>
 * 使用：在需要自定义Springboot的响应体时，调用此工具类即可
 */
@Slf4j
public class ResponseUtil {


    /**
     * 响应成功 HTTP status 200
     * <p>
     * 业务成功
     * <br>- code: 200
     * <br>- message: 请求成功 {@link BizStatus#SUCCESS}
     * <br>- data:null
     * @return ResponseEntity 包裹的标准响应体
     */
    public static <T> ResponseEntity<ApiResponse<T>> ok() {
        return ResponseEntity.ok(ApiResponse.success());
    }

    /**
     * 响应成功 HTTP status 200
     * <p>
     * 业务成功 BizStatus 200
     * @param message 业务响应体成功信息
     * @return ResponseEntity 包裹的标准响应体
     */
    public static <T> ResponseEntity<ApiResponse<T>> ok(String message) {
        return ResponseEntity.ok(ApiResponse.success(message, null));
    }

    /**
     * 响应成功 HTTP status 200
     * <p>
     * 业务失败
     * @param bizStatus 业务状态码 {@link BizStatus}
     * @param message 业务响应体错误信息
     * @return ResponseEntity 包裹的标准响应体
     */
    public static <T> ResponseEntity<ApiResponse<T>> okWithFailBody(BizStatus bizStatus,String message) {
        return ResponseEntity.ok(ApiResponse.build(bizStatus, message, null));
    }

    /**
     * 响应成功 HTTP status 200（自定义业务返回信息）
     * @param apiResponse 业务响应体
     * <br>- code 业务状态码 {@link BizStatus}
     * <br>- message 自定义成功提示信息
     * <br>- data 响应数据内容
     * @param <T> 数据类型
     * @return ResponseEntity 包裹的标准响应体
     */
    public static <T> ResponseEntity<ApiResponse<T>> okWithBizBody(ApiResponse<T> apiResponse) {
        return ResponseEntity.ok(apiResponse);
    }


    /**
     * 请求响应
     * <p>自定义HTTP状态码（{@link HttpStatus}）和响应体（{@link ApiResponse}）的内容
     * @param httpStatus http网络级状态码（如 401、404）
     * @param apiResponse 业务响应体
     * @param <T> 数据类型
     * @return ResponseEntity 包裹的失败响应体
     */
    public static <T> ResponseEntity<ApiResponse<T>> status(HttpStatus httpStatus, ApiResponse<T> apiResponse) {
        return ResponseEntity.status(httpStatus).body(apiResponse);
    }

}
