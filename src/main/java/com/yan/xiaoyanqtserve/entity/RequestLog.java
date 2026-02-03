package com.yan.xiaoyanqtserve.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * API请求日志表
 * </p>
 *
 * @author 烟尘
 * @since 2026-02-03
 */
@Getter
@Setter
@ToString
@TableName("request_log")
public class RequestLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 本次请求使用的密钥
     */
    @TableField("key_value_used")
    private String keyValueUsed;

    /**
     * 发起请求的客户端IP地址 (支持IPv4/v6)
     */
    @TableField("client_ip")
    private String clientIp;

    /**
     * 请求时间
     */
    @TableField("request_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime requestTime;

    /**
     * HTTP响应状态码，例如 200, 402, 401
     */
    @TableField("response_code")
    private Integer responseCode;

    /**
     * 处理结果摘要或错误信息
     */
    @TableField("processing_result")
    private String processingResult;
}
