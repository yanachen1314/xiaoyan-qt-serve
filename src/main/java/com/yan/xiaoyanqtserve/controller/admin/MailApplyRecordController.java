package com.yan.xiaoyanqtserve.controller.admin;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yan.xiaoyanqtserve.common.enums.ApplyStatus;
import com.yan.xiaoyanqtserve.common.utils.ApiResponse;
import com.yan.xiaoyanqtserve.entity.MailApplyRecord;
import com.yan.xiaoyanqtserve.service.MailApplyRecordService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;

/**
 * 免费领取卡密记录控制器
 * 提供邮箱申领日志的分页查询功能
 *
 * @author 烟尘
 * @since 2026-04-24
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/mailApplyRecord")
@RequiredArgsConstructor
public class MailApplyRecordController {

    private final MailApplyRecordService mailApplyRecordService;

    /**
     * 获取邮箱申领日志分页列表
     *
     * @param pageRequest 分页查询参数
     * @return 分页结果
     */
    @PostMapping("/list")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Page<MailApplyRecord>> list(@Valid @RequestBody MailApplyRecordPageRequest pageRequest) {
        Page<MailApplyRecord> page = new Page<>(pageRequest.getCurrentPage(), pageRequest.getPageSize());
        
        LambdaQueryWrapper<MailApplyRecord> lambdaQuery = new LambdaQueryWrapper<>();
        
        if (StrUtil.isNotBlank(pageRequest.getApplicantEmail())) {
            lambdaQuery.like(MailApplyRecord::getApplicantEmail, pageRequest.getApplicantEmail());
        }
        
        if (pageRequest.getApplyStatus() != null) {
            lambdaQuery.eq(MailApplyRecord::getApplyStatus, pageRequest.getApplyStatus());
        }
        
        if (StrUtil.isNotBlank(pageRequest.getDeviceType())) {
            lambdaQuery.eq(MailApplyRecord::getDeviceType, pageRequest.getDeviceType());
        }
        
        if (StrUtil.isNotBlank(pageRequest.getDeviceId())) {
            lambdaQuery.eq(MailApplyRecord::getDeviceId, pageRequest.getDeviceId());
        }
        
        if (StrUtil.isNotBlank(pageRequest.getClientIp())) {
            lambdaQuery.eq(MailApplyRecord::getClientIp, pageRequest.getClientIp());
        }
        
        if (StrUtil.isNotBlank(pageRequest.getApplyApiKey())) {
            lambdaQuery.eq(MailApplyRecord::getApplyApiKey, pageRequest.getApplyApiKey());
        }
        
        if (pageRequest.getStartTime() != null) {
            lambdaQuery.ge(MailApplyRecord::getCreatedTime, pageRequest.getStartTime());
        }
        if (pageRequest.getEndTime() != null) {
            lambdaQuery.le(MailApplyRecord::getCreatedTime, pageRequest.getEndTime());
        }
        
        lambdaQuery.orderByDesc(MailApplyRecord::getCreatedTime);
        
        Page<MailApplyRecord> result = mailApplyRecordService.page(page, lambdaQuery);
        return ApiResponse.success(result);
    }

    /**
     * 邮箱申领日志分页查询请求参数
     */
    @Data
    public static class MailApplyRecordPageRequest {
        private Integer currentPage = 1;
        private Integer pageSize = 20;
        private String applicantEmail;
        private Byte applyStatus;
        private String deviceType;
        private String deviceId;
        private String clientIp;
        private String applyApiKey;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
    }
}
