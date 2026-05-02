package com.yan.xiaoyanqtserve.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 邮件发送统计表
 * </p>
 * @author 烟尘
 * @since 2026-04-25
 */
@Getter
@Setter
@ToString
@TableName("mail_send_record")
public class MailSendRecord implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 系统发送者邮箱
     */
    @TableField("system_sender_email")
    private String systemSenderEmail;

    /**
     * 接收邮箱
     */
    @TableField("send_to_email")
    private String sendToEmail;

    /**
     * 结果状态，0：失败，1：成功
     */
    @TableField("status")
    private Integer status;

    /**
     * 结果信息
     */
    @TableField("result_msg")
    private String resultMsg;

    /**
     * 创建时间
     */
    @TableField("created_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createdTime;
}
