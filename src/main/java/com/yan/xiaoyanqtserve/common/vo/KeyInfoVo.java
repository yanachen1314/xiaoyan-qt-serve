package com.yan.xiaoyanqtserve.common.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 密钥信息表 详情VO
 * @author yanchen
 * @since 2026/3/27 21:26
 */
@Data
public class KeyInfoVo {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 32位密钥值，唯一索引
     */
    private String keyValue;

    /**
     * 密钥状态: 0-禁用, 1-启用
     */
    private Byte status;
    /**
     * 密钥销售状态（主要针对非链动小铺场景）：0-未销售，1-已销售
     */
    private Byte selled;

    /**
     * 密钥使用场景,用途
     */
    private String usageType;

    /**
     * 对应产品ID
     */
    private Integer productType;
    /**
     * 对应产品名称
     */
    private String productName;
    /**
     * 权益时间（单位分钟），转换成对应的字符串
     * 示例：10分钟，1小时，1天
     */
    private String productEquityTime;

    /**
     * 总使用次数(降AI)，0表示无限制
     */
    private Integer totalUses;

    /**
     * 剩余使用次数(降AI)，表示已用完或无限制。
     * total_uses>0时，re_uses=0表示已用完，re_uses>0表示剩余使用次数；total_uses=0时，re_uses必须设置0表示无限制
     */
    private Integer remainingUses;

    /**
     * 激活次数，0-无次数限制
     */
    private Integer activationsNum;

    /**
     * 剩余激活次数，表示已用完或无限制。
     * activeNum>0时，reNum=0表示已用完，reNum>0表示剩余激活次数；activeNum=0时，reNum必须设置0表示无限制
     */
    private Integer remainingActiveNum;

    /**
     * 激活状态: 0-未激活, 1-已激活
     */
    private Byte activated;

    /**
     * 激活设备，多个用逗号分隔
     */
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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime expiryTime;

    /**
     * 备注信息
     */
    private String remark;

    /**
     * 创建该密钥的管理员ID，关联admin_user表
     */
    private Long createdBy;
    /**
     * 创建该密钥的管理员名称
     */
    private String createdByName;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updatedTime;
}
