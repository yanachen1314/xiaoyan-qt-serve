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
 * 密钥信息表
 * </p>
 *
 * @author 烟尘
 * @since 2026-02-03
 */
@Getter
@Setter
@ToString
@TableName("key_info")
public class KeyInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 32位密钥值，唯一索引
     */
    @TableField("key_value")
    private String keyValue;

    /**
     * 密钥状态: 0-禁用, 1-启用
     */
    @TableField("status")
    private Byte status;

    /**
     * 总使用次数，0表示无限制
     */
    @TableField("total_uses")
    private Integer totalUses;

    /**
     * 剩余使用次数，0表示无限制或已用完
     */
    @TableField("remaining_uses")
    private Integer remainingUses;

    /**
     * 产品类型或套餐标识，例如: "ai_rewrite_vip", "ai_rewrite_basic"
     */
    @TableField("product_type")
    private String productType;

    /**
     * 激活时间
     */
    @TableField("activated_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime activatedTime;

    /**
     * 过期时间，可为空表示永不过期
     */
    @TableField("expiry_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime expiryTime;

    /**
     * 备注信息
     */
    @TableField("notes")
    private String notes;

    /**
     * 创建该密钥的管理员ID，关联admin_user表
     */
    @TableField("admin_created_by")
    private Long adminCreatedBy;

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
