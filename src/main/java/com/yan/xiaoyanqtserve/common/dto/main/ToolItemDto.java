package com.yan.xiaoyanqtserve.common.dto.main;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 工具项 DTO
 *
 * @author yanchen
 * @since 2026-06-06
 */
@Data
public class ToolItemDto {
    /**
     * 主键 ID（新增时不需要）
     */
    private Long id;

    /**
     * 分类ID
     */
    @NotNull(message = "分类ID不能为空")
    private Long categoryId;

    /**
     * 工具名称
     */
    @NotBlank(message = "工具名称不能为空")
    private String name;

    /**
     * 工具描述
     */
    private String description;

    /**
     * 链接地址
     */
    @NotBlank(message = "链接地址不能为空")
    private String url;

    /**
     * 图标（图标名或图片URL）
     */
    private String icon;

    /**
     * 是否热门：0-否，1-是
     */
    @NotNull(message = "热门状态不能为空")
    @Min(value = 0, message = "热门状态最小值为 0")
    private Integer hot;

    /**
     * 是否官方：0-否，1-是
     */
    @NotNull(message = "官方状态不能为空")
    @Min(value = 0, message = "官方状态最小值为 0")
    private Integer official;

    /**
     * 标签（JSON数组）
     */
    private String tags;

    /**
     * 排序序号
     */
    @NotNull(message = "排序不能为空")
    @Min(value = 0, message = "排序不能小于 0")
    private Integer sortOrder;

    /**
     * 状态：0-禁用，1-启用
     */
    @NotNull(message = "状态不能为空")
    @Min(value = 0, message = "状态最小值为 0")
    private Integer status;
}
