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
@AllArgsConstructor
@NoArgsConstructor
public class KeyInfo implements Serializable {

    @Serial
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
     * 产品类型或套餐标识，例如: "ai_rewrite_vip", "ai_rewrite_basic"
     */
    @TableField("product_type")
    private String productType;

    /**
     * 总使用次数(降AI)，0表示无限制
     */
    @TableField("total_uses")
    private Integer totalUses;

    /**
     * 剩余使用次数(降AI)，表示已用完或无限制。
     * total_uses>0时，re_uses=0表示已用完，re_uses>0表示剩余使用次数；total_uses=0时，re_uses必须设置0表示无限制
     */
    @TableField("remaining_uses")
    private Integer remainingUses;

    /**
     * 激活次数，0-无次数限制
     */
    @TableField("activations_num")
    private Integer activationsNum;

    /**
     * 剩余激活次数，表示已用完或无限制。
     * activeNum>0时，reNum=0表示已用完，reNum>0表示剩余激活次数；activeNum=0时，reNum必须设置0表示无限制
     */
    @TableField("remaining_active_num")
    private Integer remainingActiveNum;

    /**
     * 激活状态: 0-未激活, 1-已激活
     */
    @TableField("activated")
    private Byte activated;

    /**
     * 激活设备
     */
    @TableField("activation_device")
    private String activationDevice;

    /**
     * 激活时间
     */
    @TableField("activation_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime activationTime;

    /**
     * 过期时间，可为空表示永不过期
     */
    @TableField("expiry_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime expiryTime;

    /**
     * 备注信息
     */
    @TableField("remark")
    private String remark;

    /**
     * 创建该密钥的管理员ID，关联admin_user表
     */
    @TableField("created_by")
    private Long createdBy;

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
