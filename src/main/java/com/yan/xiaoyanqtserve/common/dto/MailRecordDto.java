package com.yan.xiaoyanqtserve.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 邮件发送卡密记录dto
 * @author yanchen
 * @since 2026/4/24 21:32
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MailRecordDto {

    /**
     * 发送卡密的系统邮箱
     */
    private String systemSenderEmail;

    /**
     * 发送结果状态（0：失败，1：成功）
     */
    private Integer resultStatus;
    /**
     * 发送结果信息（成功或者失败的原因）
     */
    private String resultMsg;


    /**
     * 领取者邮箱
     */
    private String applicantEmail;

    /**
     * 领取的卡密
     */
    private String licenseKey;

    /**
     * 设备类型（web：网页端，app：win APP端）
     */
    private String deviceType;

    /**
     * 设备ID/设备指纹
     */
    private String deviceId;

    /**
     * 领取时的客户端IP地址（支持IPv4/IPv6）
     */
    private String clientIp;

    /**
     * 用户代理信息（浏览器/客户端标识）
     */
    private String userAgent;
}
