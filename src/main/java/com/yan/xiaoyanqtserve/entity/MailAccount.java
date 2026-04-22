package com.yan.xiaoyanqtserve.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 邮件账号配置表
 * @author yanchen
 * @since 2026/4/22
 */
@Getter
@Setter
@ToString
@TableName("mail_account")
@AllArgsConstructor
@NoArgsConstructor
public class MailAccount implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("email_address")
    private String emailAddress;

    @TableField("host")
    private String host;

    @TableField("port")
    private Integer port;

    @TableField("password")
    private String password;

    @TableField("protocol")
    private String protocol;

    @TableField("ssl_enable")
    private Byte sslEnable;

    @TableField("status")
    private Byte status;

    @TableField("daily_limit")
    private Integer dailyLimit;

    @TableField("priority")
    private Integer priority;

    @TableField("remark")
    private String remark;

    @TableField("created_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createdTime;

    @TableField("updated_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updatedTime;
}