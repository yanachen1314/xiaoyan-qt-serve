package com.yan.xiaoyanqtserve.controller.admin;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yan.xiaoyanqtserve.common.dto.requestLog.RequestLogPageRequest;
import com.yan.xiaoyanqtserve.common.utils.ApiResponse;
import com.yan.xiaoyanqtserve.entity.RequestLog;
import com.yan.xiaoyanqtserve.service.RequestLogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * API 请求日志表 前端控制器
 * 提供请求日志的分页查询功能
 *
 * @author 烟尘
 * @since 2026-02-03
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/requestLog")
@RequiredArgsConstructor
public class RequestLogController {

    private final RequestLogService requestLogService;

    /**
     * 获取请求日志分页列表
     *
     * @param pageRequest 分页查询参数
     * @return 分页结果
     */
    @PostMapping("/list")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Page<RequestLog>> list(@Valid @RequestBody RequestLogPageRequest pageRequest) {
        Page<RequestLog> requestLogPage = new Page<>(pageRequest.getCurrentPage(), pageRequest.getPageSize());
        
        // 使用 lambdaQuery 构建查询条件
        LambdaQueryWrapper<RequestLog> lambdaQuery = new LambdaQueryWrapper<>();
        
        // 请求 ID 模糊查询
        if (StrUtil.isNotBlank(pageRequest.getRequestId())) {
            lambdaQuery.like(RequestLog::getRequestId, pageRequest.getRequestId());
        }
        
        // 请求者模糊查询
        if (StrUtil.isNotBlank(pageRequest.getRequester())) {
            lambdaQuery.like(RequestLog::getRequester, pageRequest.getRequester());
        }
        
        // 请求类型精确查询
        if (StrUtil.isNotBlank(pageRequest.getRequestType())) {
            lambdaQuery.eq(RequestLog::getRequestType, pageRequest.getRequestType());
        }
        
        // 客户端 IP 精确查询
        if (StrUtil.isNotBlank(pageRequest.getClientIp())) {
            lambdaQuery.eq(RequestLog::getClientIp, pageRequest.getClientIp());
        }
        
        // HTTP 状态码精确查询
        if (pageRequest.getHttpCode() != null) {
            lambdaQuery.eq(RequestLog::getHttpCode, pageRequest.getHttpCode());
        }
        
        // 时间范围查询
        if (pageRequest.getStartTime() != null) {
            lambdaQuery.ge(RequestLog::getRequestTime, pageRequest.getStartTime());
        }
        if (pageRequest.getEndTime() != null) {
            lambdaQuery.le(RequestLog::getRequestTime, pageRequest.getEndTime());
        }
        
        // 按请求时间倒序排列
        lambdaQuery.orderByDesc(RequestLog::getRequestTime);
        
        Page<RequestLog> result = requestLogService.page(requestLogPage, lambdaQuery);
        return ApiResponse.success(result);
    }
}
