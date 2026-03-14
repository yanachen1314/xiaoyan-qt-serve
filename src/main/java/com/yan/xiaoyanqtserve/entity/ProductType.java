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
 * 产品信息表
 * </p>
 *
 * @author 烟尘
 * @since 2026-03-14
 */
@Getter
@Setter
@ToString
@TableName("product_type")
public class ProductType implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 产品名称
     */
    @TableField("product_name")
    private String productName;

    /**
     * 产品编码
     */
    @TableField("product_code")
    private String productCode;

    /**
     * 产品所用权益价值，单位：分钟
     */
    @TableField("equity_time")
    private Integer equityTime;

    /**
     * 产品所代表的使用次数（降AI），0：无限制，>0：有限使用次数
     */
    @TableField("equity_usage_num")
    private Integer equityUsageNum;

    /**
     * 产品所代表的卡密激活次数，0：无限制，>0：有限激活次数
     */
    @TableField("equity_active_num")
    private Integer equityActiveNum;

    /**
     * 产品描述
     */
    @TableField("description")
    private String description;

    /**
     * 产品状态: 0-禁用, 1-启用
     */
    @TableField("status")
    private Byte status;

    /**
     * 排序序号
     */
    @TableField("sort_order")
    private Integer sortOrder;

    /**
     * 创建时间
     */
    @TableField("create_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField("update_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;
}
