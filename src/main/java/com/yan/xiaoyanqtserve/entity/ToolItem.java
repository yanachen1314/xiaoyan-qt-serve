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
 * 工具项表
 *
 * @author yanchen
 * @since 2026-06-06
 */
@Getter
@Setter
@ToString
@TableName("tool_item")
public class ToolItem implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 分类ID
     */
    @TableField("category_id")
    private Long categoryId;

    /**
     * 工具名称
     */
    @TableField("name")
    private String name;

    /**
     * 工具描述
     */
    @TableField("description")
    private String description;

    /**
     * 链接地址
     */
    @TableField("url")
    private String url;

    /**
     * 图标（图标名或图片URL）
     */
    @TableField("icon")
    private String icon;

    /**
     * 是否热门：0-否，1-是
     */
    @TableField("hot")
    private Integer hot;

    /**
     * 是否官方：0-否，1-是
     */
    @TableField("official")
    private Integer official;

    /**
     * 标签（JSON数组）
     */
    @TableField("tags")
    private String tags;

    /**
     * 点击次数
     */
    @TableField("click_count")
    private Long clickCount;

    /**
     * 排序序号
     */
    @TableField("sort_order")
    private Integer sortOrder;

    /**
     * 状态：0-禁用，1-启用
     */
    @TableField("status")
    private Integer status;

    /**
     * 创建时间
     */
    @TableField("created_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField("updated_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updatedAt;
}
