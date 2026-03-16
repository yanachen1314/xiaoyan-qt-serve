package com.yan.xiaoyanqtserve.common.dto.requestLog;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.yan.xiaoyanqtserve.common.dto.adminUser.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 请求日志分页查询参数
 *
 * @author 烟尘
 * @since 2026-02-03
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RequestLogPageRequest extends PageRequest {
    /**
     * 密钥关键词（模糊查询）
     */
    private String keyword;

    /**
     * 客户端 IP 地址
     */
    private String clientIp;

    /**
     * 响应状态码
     */
    private Integer responseCode;

    /**
     * 开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime endTime;
}
