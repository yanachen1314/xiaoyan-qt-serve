package com.yan.xiaoyanqtserve.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.io.Serial;
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
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RequestLog implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 请求唯一UUID
     */
    @TableField("request_id")
    private String requestId;

    /**
     * 请求者
     */
    @TableField("requester")
    private String requester;

    /**
     * 请求接口
     */
    @TableField("request_url")
    private String requestUrl;


    /**
     * 发起请求的客户端IP地址 (支持IPv4/v6)
     */
    @TableField("client_ip")
    private String clientIp;

    /**
     * 请求类别，如：公开请求、管理员请求、卡密请求
     * @see com.yan.xiaoyanqtserve.common.enums.RequestType
     */
    @TableField("request_type")
    private String requestType;

    /**
     * 请求时间
     */
    @TableField("request_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime requestTime;

    /**
     * 请求耗时，单位毫秒
     */
    @TableField("request_duration")
    private Long requestDuration;

    /**
     * HTTP响应状态码，例如 200, 402, 401
     */
    @TableField("http_code")
    private Integer httpCode;

    /**
     * 业务状态码，例如 200, 402, 401
     */
    @TableField("biz_code")
    private Integer bizCode;

    /**
     * 处理结果摘要或错误信息
     */
    @TableField("response_message")
    private String responseMessage;

    /**
     * 输入文本
     */
    @TableField("input_text")
    private String inputText;

    /**
     * 输出文本
     */
    @TableField("output_text")
    private String outputText;

    /**
     * 输入文本的字符数
     */
    @TableField("input_text_chars")
    private Integer inputTextChars;
    /**
     * 输入文本的token数
     */
    @TableField("input_tokens")
    private Integer inputTokens;
    /**
     * 输出文本的token数
     */
    @TableField("output_tokens")
    private Integer outputTokens;
    /**
     * 本次请求使用的总token数（输入文本的token数 + 输出文本的token数）
     */
    @TableField("total_tokens")
    private Integer totalTokens;
}
