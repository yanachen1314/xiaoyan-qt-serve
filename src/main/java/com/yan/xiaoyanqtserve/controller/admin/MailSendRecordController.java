package com.yan.xiaoyanqtserve.controller.admin;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yan.xiaoyanqtserve.common.utils.ApiResponse;
import com.yan.xiaoyanqtserve.entity.MailSendRecord;
import com.yan.xiaoyanqtserve.service.MailSendRecordService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 邮件发送记录控制器
 * 提供邮件发送日志的分页查询和统计功能
 *
 * @author 烟尘
 * @since 2026-04-24
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/mailSendRecord")
@RequiredArgsConstructor
public class MailSendRecordController {

    private final MailSendRecordService mailSendRecordService;

    /**
     * 获取邮件发送日志分页列表
     *
     * @param pageRequest 分页查询参数
     * @return 分页结果
     */
    @PostMapping("/list")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Page<MailSendRecord>> list(@Valid @RequestBody MailSendRecordPageRequest pageRequest) {
        Page<MailSendRecord> page = new Page<>(pageRequest.getCurrentPage(), pageRequest.getPageSize());
        
        LambdaQueryWrapper<MailSendRecord> lambdaQuery = new LambdaQueryWrapper<>();
        
        if (StrUtil.isNotBlank(pageRequest.getSendToEmail())) {
            lambdaQuery.like(MailSendRecord::getSendToEmail, pageRequest.getSendToEmail());
        }
        
        if (StrUtil.isNotBlank(pageRequest.getSystemSenderEmail())) {
            lambdaQuery.like(MailSendRecord::getSystemSenderEmail, pageRequest.getSystemSenderEmail());
        }
        
        if (pageRequest.getStatus() != null) {
            lambdaQuery.eq(MailSendRecord::getStatus, pageRequest.getStatus());
        }
        
        if (pageRequest.getStartTime() != null) {
            lambdaQuery.ge(MailSendRecord::getCreatedTime, pageRequest.getStartTime());
        }
        if (pageRequest.getEndTime() != null) {
            lambdaQuery.le(MailSendRecord::getCreatedTime, pageRequest.getEndTime());
        }
        
        lambdaQuery.orderByDesc(MailSendRecord::getCreatedTime);
        
        Page<MailSendRecord> result = mailSendRecordService.page(page, lambdaQuery);
        return ApiResponse.success(result);
    }

    /**
     * 获取邮件发送统计（按发送者账号分组统计）
     *
     * @param statRequest 统计查询参数
     * @return 统计结果
     */
    @PostMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Map<String, Object>> statistics(@RequestBody MailSendRecordStatisticsRequest statRequest) {
        LambdaQueryWrapper<MailSendRecord> lambdaQuery = new LambdaQueryWrapper<>();
        
        if (statRequest.getStartTime() != null) {
            lambdaQuery.ge(MailSendRecord::getCreatedTime, statRequest.getStartTime());
        }
        if (statRequest.getEndTime() != null) {
            lambdaQuery.le(MailSendRecord::getCreatedTime, statRequest.getEndTime());
        }
        
        List<MailSendRecord> records = mailSendRecordService.list(lambdaQuery);
        
        Map<String, int[]> emailStats = new HashMap<>();
        int totalSent = 0;
        int totalSuccess = 0;
        int totalFailed = 0;
        
        for (MailSendRecord record : records) {
            String sender = record.getSystemSenderEmail();
            if (sender == null) {
                sender = "未知账号";
            }
            
            emailStats.putIfAbsent(sender, new int[]{0, 0, 0});
            int[] stats = emailStats.get(sender);
            stats[0]++;
            if (record.getStatus() != null && record.getStatus() == 1) {
                stats[1]++;
                totalSuccess++;
            } else {
                stats[2]++;
                totalFailed++;
            }
            totalSent++;
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("total", totalSent);
        result.put("totalSuccess", totalSuccess);
        result.put("totalFailed", totalFailed);
        
        List<String> senderEmails = new java.util.ArrayList<>(emailStats.keySet());
        List<Integer> successCounts = new java.util.ArrayList<>();
        List<Integer> failedCounts = new java.util.ArrayList<>();
        
        for (String email : senderEmails) {
            int[] stats = emailStats.get(email);
            successCounts.add(stats[1]);
            failedCounts.add(stats[2]);
        }
        
        result.put("senderEmails", senderEmails);
        result.put("successCounts", successCounts);
        result.put("failedCounts", failedCounts);
        
        return ApiResponse.success(result);
    }

    /**
     * 邮件发送日志分页查询请求参数
     */
    @Data
    public static class MailSendRecordPageRequest {
        private Integer currentPage = 1;
        private Integer pageSize = 20;
        private String sendToEmail;
        private String systemSenderEmail;
        private Integer status;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
    }

    /**
     * 邮件发送统计查询请求参数
     */
    @Data
    public static class MailSendRecordStatisticsRequest {
        private LocalDateTime startTime;
        private LocalDateTime endTime;
    }
}
