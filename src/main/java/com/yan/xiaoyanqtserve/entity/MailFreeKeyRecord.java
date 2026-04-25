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
 * 免费领取卡密记录表
 * </p>
 *
 * @author yanchen
 * @since 2026-04-22
 */
@Getter
@Setter
@ToString
@TableName("mail_free_key_record")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MailFreeKeyRecord implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 领取邮箱
     */
    @TableField("email")
    private String email;

    /**
     * 领取的卡密
     */
    @TableField("license_key")
    private String licenseKey;

    /**
     * 设备类型（web：网页端，app：win APP端）
     */
    @TableField("device_type")
    private String deviceType;

    /**
     * 设备ID/设备指纹
     */
    @TableField("device_id")
    private String deviceId;

    /**
     * 领取时的客户端IP地址（支持IPv4/IPv6）
     */
    @TableField("client_ip")
    private String clientIp;

    /**
     * 用户代理信息（浏览器/客户端标识）
     */
    @TableField("user_agent")
    private String userAgent;

    /**
     * 领取时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField("created_time")
    private LocalDateTime createdTime;
}
