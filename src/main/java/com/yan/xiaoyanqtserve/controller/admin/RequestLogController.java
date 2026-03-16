package com.yan.xiaoyanqtserve.controller.admin;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
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
        
        QueryWrapper<RequestLog> queryWrapper = new QueryWrapper<>();
        
        // 密钥关键词模糊查询
        if (StrUtil.isNotBlank(pageRequest.getKeyword())) {
            queryWrapper.like("key_value_used", pageRequest.getKeyword());
        }
        
        // 客户端 IP 精确查询
        if (StrUtil.isNotBlank(pageRequest.getClientIp())) {
            queryWrapper.eq("client_ip", pageRequest.getClientIp());
        }
        
        // 响应状态码精确查询
        if (pageRequest.getResponseCode() != null) {
            queryWrapper.eq("response_code", pageRequest.getResponseCode());
        }
        
        // 时间范围查询
        if (pageRequest.getStartTime() != null) {
            queryWrapper.ge("request_time", pageRequest.getStartTime());
        }
        if (pageRequest.getEndTime() != null) {
            queryWrapper.le("request_time", pageRequest.getEndTime());
        }
        
        // 按请求时间倒序排列
        queryWrapper.orderByDesc("request_time");
        
        Page<RequestLog> result = requestLogService.page(requestLogPage, queryWrapper);
        return ApiResponse.success(result);
    }
}
