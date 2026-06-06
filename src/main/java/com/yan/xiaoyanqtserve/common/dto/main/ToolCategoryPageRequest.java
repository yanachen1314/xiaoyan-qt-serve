package com.yan.xiaoyanqtserve.common.dto.main;

import com.yan.xiaoyanqtserve.common.dto.adminUser.PageRequest;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 工具分类分页查询请求
 *
 * @author yanchen
 * @since 2026-06-06
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ToolCategoryPageRequest extends PageRequest {
    /**
     * 分类名称（支持模糊查询）
     */
    private String name;

    /**
     * 状态（0-禁用，1-启用）
     */
    @Min(value = 0, message = "状态最小值为 0")
    @Max(value = 1, message = "状态最大值为 1")
    private Integer status;
}
