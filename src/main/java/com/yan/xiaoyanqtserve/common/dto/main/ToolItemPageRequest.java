package com.yan.xiaoyanqtserve.common.dto.main;

import com.yan.xiaoyanqtserve.common.dto.adminUser.PageRequest;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 工具项分页查询请求
 *
 * @author yanchen
 * @since 2026-06-06
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ToolItemPageRequest extends PageRequest {
    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 工具名称（支持模糊查询）
     */
    private String name;

    /**
     * 状态（0-禁用，1-启用）
     */
    @Min(value = 0, message = "状态最小值为 0")
    @Max(value = 1, message = "状态最大值为 1")
    private Integer status;

    /**
     * 是否热门（0-否，1-是）
     */
    @Min(value = 0, message = "热门状态最小值为 0")
    @Max(value = 1, message = "热门状态最大值为 1")
    private Integer hot;

    /**
     * 是否官方（0-否，1-是）
     */
    @Min(value = 0, message = "官方状态最小值为 0")
    @Max(value = 1, message = "官方状态最大值为 1")
    private Integer official;
}
