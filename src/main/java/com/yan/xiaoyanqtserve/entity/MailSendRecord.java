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
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 邮件发送统计表
 * @author yanchen
 * @since 2026/4/22
 */
@Getter
@Setter
@ToString
@TableName("mail_send_record")
@AllArgsConstructor
@NoArgsConstructor
public class MailSendRecord implements Serializable {

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
     * 发送日期
     */
    @TableField("send_date")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate sendDate;

    /**
     * 发送数量
     */
    @TableField("send_count")
    private Integer sendCount;

    /**
     * 成功数量
     */
    @TableField("success_count")
    private Integer successCount;

    /**
     * 失败数量
     */
    @TableField("fail_count")
    private Integer failCount;

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