package com.yan.xiaoyanqtserve.common.exception;

import com.yan.xiaoyanqtserve.common.Constants;
import com.yan.xiaoyanqtserve.common.enums.BizStatus;
import com.yan.xiaoyanqtserve.common.utils.ApiResponse;
import com.yan.xiaoyanqtserve.common.utils.ResponseUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.UncategorizedSQLException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.nio.file.AccessDeniedException;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 全局异常处理器
 *
 * @author yanchen
 * @since 2021/1/28 16:06
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ----------------------------------- 安全异常处理器（ Spring Security 相关异常） -----------------------------------

    /**
     * 无权限（已登录，但没有访问权限）
     */
    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<?> securityAccessDeniedExceptionHandler(org.springframework.security.access.AccessDeniedException e, HttpServletRequest request) {
        log.warn("请求地址：{}，权限不足：{}", request.getRequestURI(), e.getMessage());
        ApiResponse<?> apiResponse = ApiResponse.build(BizStatus.FORBIDDEN, BizStatus.FORBIDDEN.msg(), null);
        setBizInfoToRequest(request, apiResponse.getCode(), apiResponse.getMessage());
        return ResponseUtil.status(HttpStatus.FORBIDDEN, apiResponse);
    }

    /**
     * 未认证（未登录 / token 失效）
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<?> securityAuthenticationExceptionHandler(AuthenticationException e, HttpServletRequest request) {
        log.warn("请求地址：{}，未认证：{}", request.getRequestURI(), e.getMessage());
        ApiResponse<?> apiResponse = ApiResponse.build(BizStatus.UNAUTHORIZED, "未登录或 Token 无效", null);
        setBizInfoToRequest(request, apiResponse.getCode(), apiResponse.getMessage());
        return ResponseUtil.status(HttpStatus.UNAUTHORIZED, apiResponse);
    }

    /**
     * 登录失败（用户名/密码错误）
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<?> securityBadCredentialsHandler(BadCredentialsException e, HttpServletRequest request) {
        log.warn("请求地址：{}，登录失败：{}", request.getRequestURI(), e.getMessage());
        ApiResponse<?> apiResponse = ApiResponse.build(BizStatus.UNAUTHORIZED, "用户名或密码错误", null);
        setBizInfoToRequest(request, apiResponse.getCode(), apiResponse.getMessage());
        return ResponseUtil.status(HttpStatus.UNAUTHORIZED, apiResponse);
    }

    // ----------------------------------- 请求参数异常处理 -----------------------------------

    /**
     * 拦截非法参数异常
     * @param e 异常(IllegalArgumentException)
     * @param request request
     * @return ResponseUtil
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> argumentIllegalArgumentExceptionHandler(IllegalArgumentException e,
                                                                     HttpServletRequest request) {
        String message = e.getMessage();
        log.error("请求地址：{}，发生非法参数异常：{}", request.getRequestURI(), message, e);
        ApiResponse<?> apiResponse = ApiResponse.build(BizStatus.BAD_REQUEST, message != null ? message : "非法参数", null);
        setBizInfoToRequest(request, apiResponse.getCode(), apiResponse.getMessage());
        return ResponseUtil.okWithFailBody(BizStatus.BAD_REQUEST, message != null ? message : "非法参数");
    }

    /**
     * 请求方式不支持
     * @param e 请求方式不支持
     * @param request request
     * @return ResponseUtil
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<?> argumentHttpRequestMethodNotSupportedHandler(HttpRequestMethodNotSupportedException e,
                                                                          HttpServletRequest request) {
        log.error("请求地址: {}, 不支持{}请求", request.getRequestURI(), e.getMethod());
        String message = e.getMessage(); // 可以构建更友好的消息
        ApiResponse<?> apiResponse = ApiResponse.build(BizStatus.BAD_REQUEST, message, null);
        setBizInfoToRequest(request, apiResponse.getCode(), apiResponse.getMessage());
        return ResponseUtil.okWithFailBody(BizStatus.BAD_REQUEST, message);
    }

    /**
     * 处理 JSON 解析失败（包括 @JsonCreator 抛出的异常）
     * @param e JSON 解析失败
     * @param request request
     * @return ResponseUtil
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<?> handleHttpMessageNotReadable(
            HttpMessageNotReadableException e, HttpServletRequest request) {
        log.error("请求地址: {}, JSON 解析失败: {}", request.getRequestURI(), e.getMessage(), e);

        String message = "参数格式错误";
        Throwable cause = e.getCause();

        // 👇 递归查找 BizException
        while (cause != null) {
            if (cause instanceof BizException) {
                message = cause.getMessage();
                break;
            }
            cause = cause.getCause(); // 继续深入
        }

        // 如果没找到 BizException，可选择使用最内层异常（可选）
        if ("参数格式错误".equals(message)) {
            cause = e;
            while (cause.getCause() != null) {
                cause = cause.getCause();
            }
            message = cause.getMessage() != null ? cause.getMessage() : "参数格式错误";
        }

        ApiResponse<?> apiResponse = ApiResponse.build(BizStatus.BAD_REQUEST, message, null);
        setBizInfoToRequest(request, apiResponse.getCode(), apiResponse.getMessage());
        return ResponseUtil.okWithFailBody(BizStatus.BAD_REQUEST, message);
    }

    /**
     * 请求参数的类型与处理器方法参数类型不匹配：比如形参是int，传入string
     * @param e 参数校验异常
     * @param request request
     * @return ResponseUtil
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<?> argumentMethodArgumentTypeMismatchExceptionHandler(MethodArgumentTypeMismatchException e,
                                                                                HttpServletRequest request) {
        log.error("1、请求地址：{}，参数验证失败：{}", request.getRequestURI(), e.getMessage(), e);
        String message = "参数类型传递错误，" +
                "参数类型：" + Objects.requireNonNull(e.getRequiredType()).getName() + " " +
                "传入类型：" + Objects.requireNonNull(e.getRootCause()).getMessage();
        ApiResponse<?> apiResponse = ApiResponse.build(BizStatus.BAD_REQUEST, message, null);
        setBizInfoToRequest(request, apiResponse.getCode(), apiResponse.getMessage());
        return ResponseUtil.okWithFailBody(BizStatus.BAD_REQUEST, message);
    }

    /**
     * 对接口方法形参的校验没有通过：@Length、@Email、、、、
     * @param e 参数校验异常
     * @param request request
     * @return ResponseUtil
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<?> argumentConstraintViolationExceptionHandler(ConstraintViolationException e, HttpServletRequest request) {
        String message = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));
        log.error("message:{}", message);
        log.error("2、请求地址：{}，参数验证失败：{}", request.getRequestURI(), e.getMessage(), e);
        ApiResponse<?> apiResponse = ApiResponse.build(BizStatus.BAD_REQUEST, message, null);
        setBizInfoToRequest(request, apiResponse.getCode(), apiResponse.getMessage());
        return ResponseUtil.okWithFailBody(BizStatus.BAD_REQUEST, message);
    }

    /**
     * 对实体类的属性进行校验时没有通过：@Length、@Email、、、、
     * MethodArgumentNotValidException 异常类继承自该异常类，功能一致，处理异常的逻辑完全参考
     * @param e 参数校验异常
     * @param request request
     * @return ResponseUtil
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<?> argumentBindExceptionHandler(BindException e, HttpServletRequest request) {
        log.error("3、请求地址：{}，参数验证失败：{}", request.getRequestURI(), e.getMessage(), e);
        String message = e.getAllErrors().get(0).getDefaultMessage();
        ApiResponse<?> apiResponse = ApiResponse.build(BizStatus.BAD_REQUEST, message, null);
        setBizInfoToRequest(request, apiResponse.getCode(), apiResponse.getMessage());
        return ResponseUtil.okWithFailBody(BizStatus.BAD_REQUEST, message);
    }

    /**
     * 参数验证失败异常
     * 对实体类的属性进行校验时没有通过：@Length、@Email、、、、
     * @param e 参数验证失败异常
     * @param request request
     * @return ResponseUtil
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> argumentMethodArgumentNotValidExceptionHandler(MethodArgumentNotValidException e,
                                                                            HttpServletRequest request) {
        String message = e.getBindingResult().getAllErrors().stream()
                .findFirst()
                .map(ObjectError::getDefaultMessage)
                .orElse("参数校验失败");

        log.error("4、请求地址：{}，参数验证失败：{},{}", request.getRequestURI(), e.getObjectName(), message, e);
        ApiResponse<?> apiResponse = ApiResponse.build(BizStatus.BAD_REQUEST, message, null);
        setBizInfoToRequest(request, apiResponse.getCode(), apiResponse.getMessage());
        return ResponseUtil.okWithFailBody(BizStatus.BAD_REQUEST, message);
    }

    /**
     * 请求参数缺失，没有传入必要的参数
     * @param e 参数校验异常
     * @param request request
     * @return ResponseUtil
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<?> argumentMissingServletRequestParameterExceptionHandler(MissingServletRequestParameterException e,
                                                                                    HttpServletRequest request) {
        log.error("5、请求地址：{},参数校验失败：{}", request.getRequestURI(), e.getMessage(), e);
        ApiResponse<?> apiResponse = ApiResponse.build(BizStatus.BAD_REQUEST, e.getMessage(), null);
        setBizInfoToRequest(request, apiResponse.getCode(), apiResponse.getMessage());
        return ResponseUtil.okWithFailBody(BizStatus.BAD_REQUEST, e.getMessage());
    }

    // ----------------------------------- 数据库异常处理 -----------------------------------

    /**
     * 拦截错误SQL异常
     * @param e 异常
     * @param request request
     * @return ResponseUtil
     */
    @ExceptionHandler(BadSqlGrammarException.class)
    public ResponseEntity<?> sqlBadSqlGrammarExceptionHandler(BadSqlGrammarException e,
                                                              HttpServletRequest request) {
        log.error("数据库异常1、请求地址：{}，发生数据库异常：SQL错误,{}", request.getRequestURI(), e.getSql(), e);
        ApiResponse<?> apiResponse = ApiResponse.build(BizStatus.ERROR, "数据异常：SQL错误", null);
        setBizInfoToRequest(request, apiResponse.getCode(), apiResponse.getMessage());
        return ResponseUtil.okWithFailBody(BizStatus.ERROR, "数据异常：SQL错误");
    }

    /**
     * 可以拦截表示违反数据库的完整性约束导致的异常。
     * @param e 异常
     * @param request request
     * @return ResponseUtil
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<?> sqlDataIntegrityViolationExceptionHandler(DataIntegrityViolationException e,
                                                                       HttpServletRequest request) {
        String message = e.getCause().getMessage();
        log.error("数据库异常2、请求地址：{}，发生数据库异常:{}", request.getRequestURI(), message, e);
        ApiResponse<?> apiResponse = ApiResponse.build(BizStatus.ERROR, "数据异常：" + message, null);
        setBizInfoToRequest(request, apiResponse.getCode(), apiResponse.getMessage());
        return ResponseUtil.okWithFailBody(BizStatus.ERROR, "数据异常：" + message);
    }

    /**
     * 可以拦截违反数据库的非完整性约束导致的异常，可能也会拦截一些也包括 SQL 语句错误、连接问题、权限问题等各种数据库异常。
     * @param e 异常
     * @param request request
     * @return ResponseUtil
     */
    @ExceptionHandler(UncategorizedSQLException.class)
    public ResponseEntity<?> sqlUncategorizedSqlExceptionHandler(UncategorizedSQLException e,
                                                                 HttpServletRequest request) {
        log.error("数据库异常3、请求地址：{}，发生数据库异常:{}", request.getRequestURI(), e.getSql(), e);
        ApiResponse<?> apiResponse = ApiResponse.build(BizStatus.ERROR, "数据异常！", null);
        setBizInfoToRequest(request, apiResponse.getCode(), apiResponse.getMessage());
        return ResponseUtil.okWithFailBody(BizStatus.ERROR, "数据异常！");
    }

    // ----------------------------------- 文件异常处理 -----------------------------------

    /**
     * 文件 IO 时权限不足
     *
     * @param e       权限校验异常
     * @param request request
     * @return ResponseUtil
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> FileAccessDeniedExceptionHandler(AccessDeniedException e,
                                                              HttpServletRequest request) {
        log.error("请求地址：{},权限校验失败：{}", request.getRequestURI(), e.getMessage());
        ApiResponse<?> apiResponse = ApiResponse.build(BizStatus.FORBIDDEN, "文件操作，权限不足！", null);
        setBizInfoToRequest(request, apiResponse.getCode(), apiResponse.getMessage());
        return ResponseUtil.okWithFailBody(BizStatus.FORBIDDEN, "文件操作，权限不足！");
    }


    // ----------------------------------- 全局异常处理 -----------------------------------

    /**
     * 业务自定义异常
     *
     * @param e       业务自定义异常
     * @param request request
     * @return ResponseUtil
     */
    @ExceptionHandler(BizException.class)
    public ResponseEntity<?> globalServiceExceptionHandler(BizException e,
                                                           HttpServletRequest request) {
        log.error("请求地址：{}，发生业务自定义异常：{}", request.getRequestURI(), e.getMessage(), e);
        ApiResponse<?> apiResponse = ApiResponse.build(BizStatus.ERROR, e.getMessage(), null);
        setBizInfoToRequest(request, apiResponse.getCode(), apiResponse.getMessage());
        return ResponseUtil.okWithFailBody(BizStatus.ERROR, e.getMessage());
    }

    /**
     * 拦截未知的运行时异常
     *
     * @param e       异常(RuntimeException)
     * @param request request
     * @return ResponseUtil
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> globalRuntimeExceptionHandler(RuntimeException e,
                                                           HttpServletRequest request) {
        log.error("请求地址：{}，发生未知的运行时异常：{}", request.getRequestURI(), e.getMessage(), e);
        String message = e.getMessage();
        ApiResponse<?> apiResponse = ApiResponse.build(BizStatus.ERROR, message != null ? message : "系统运行时异常", null);
        setBizInfoToRequest(request, apiResponse.getCode(), apiResponse.getMessage());
        return ResponseUtil.okWithFailBody(BizStatus.ERROR, message != null ? message : "系统运行时异常");
    }

    /**
     * 全局异常
     *
     * @param e       全局异常(Exception)
     * @param request request
     * @return ResponseUtil
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> globalExceptionHandler(Exception e,
                                                    HttpServletRequest request) {
        log.error("请求地址：{}，发生系统异常", request.getRequestURI(), e);
        ApiResponse<?> apiResponse = ApiResponse.build(BizStatus.ERROR, e.getMessage(), null);
        setBizInfoToRequest(request, apiResponse.getCode(), apiResponse.getMessage());
        return ResponseUtil.okWithFailBody(BizStatus.ERROR, e.getMessage());
    }

    // --- 辅助方法：将业务信息设置到请求属性 ---
    private void setBizInfoToRequest(HttpServletRequest request, Integer code, String message) {
        request.setAttribute(Constants.ATTRIBUTE_BIZ_CODE, code);
        request.setAttribute(Constants.ATTRIBUTE_RESPONSE_MESSAGE, message);
    }
}