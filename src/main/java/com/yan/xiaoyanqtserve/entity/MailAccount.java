package com.yan.xiaoyanqtserve.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 邮件账号配置表
 * </p>
 *
 * @author 烟尘
 * @since 2026-04-24
 */
@Getter
@Setter
@ToString
@TableName("mail_account")
public class MailAccount implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 邮箱地址
     */
    @TableField("email_address")
    private String emailAddress;

    /**
     * SMTP服务器地址
     */
    @TableField("host")
    private String host;

    /**
     * SMTP端口
     */
    @TableField("port")
    private Integer port;

    /**
     * 邮箱密码/授权码
     */
    @TableField("password")
    private String password;

    /**
     * 密码或授权码创建时间
     */
    @TableField("password_create_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime passwordCreateTime;

    /**
     * 协议类型
     */
    @TableField("protocol")
    private String protocol;

    /**
     * 是否启用SSL: 0-否, 1-是
     */
    @TableField("ssl_enable")
    private Byte sslEnable;

    /**
     * 状态: 0-禁用, 1-启用
     */
    @TableField("status")
    private Byte status;

    /**
     * 每日发送上限
     */
    @TableField("daily_limit")
    private Integer dailyLimit;

    /**
     * 优先级，数值越小优先级越高
     */
    @TableField("priority")
    private Integer priority;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

    /**
     * 创建时间
     */
    @TableField("created_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    @TableField("updated_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updatedTime;
}