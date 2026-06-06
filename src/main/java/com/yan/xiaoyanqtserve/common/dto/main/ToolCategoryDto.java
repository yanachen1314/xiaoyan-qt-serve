package com.yan.xiaoyanqtserve.common.dto.main;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 工具分类 DTO
 *
 * @author yanchen
 * @since 2026-06-06
 */
@Data
public class ToolCategoryDto {
    /**
     * 主键 ID（新增时不需要）
     */
    private Long id;

    /**
     * 分类名称
     */
    @NotBlank(message = "分类名称不能为空")
    private String name;

    /**
     * 分类描述
     */
    private String description;

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
