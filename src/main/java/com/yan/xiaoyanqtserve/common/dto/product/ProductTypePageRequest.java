package com.yan.xiaoyanqtserve.common.dto.product;

import com.yan.xiaoyanqtserve.common.dto.adminUser.PageRequest;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 产品类型分页查询请求
 * @author yanchen
 * @since 2026/3/14
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProductTypePageRequest extends PageRequest {
    /**
     * 类型名称（支持模糊查询）
     */
    private String typeName;

    /**
     * 状态（0-禁用，1-启用）
     */
    @Min(value = 0, message = "状态最小值为 0")
    @Max(value = 1, message = "状态最大值为 1")
    private Byte status;
}
