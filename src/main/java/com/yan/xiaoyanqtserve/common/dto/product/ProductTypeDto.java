package com.yan.xiaoyanqtserve.common.dto.product;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 产品类型 DTO
 * @author yanchen
 * @since 2026/3/14
 */
@Data
public class ProductTypeDto {
    /**
     * 主键 ID（新增时不需要）
     */

    private Long id;

    /**
     * 产品名称
     */
    @NotBlank(message = "产品名称不能为空")
    private String productName;

    /**
     * 产品编码
     */
    @NotBlank(message = "产品编码不能为空")
    private String productCode;

    /**
     * 产品所代表的使用次数（降 AI），0：无限制
     */
    @NotNull(message = "使用次数不能为空")
    @Min(value = 0, message = "使用次数不能小于 0")
    private Integer equityUsageNum;

    /**
     * 产品所代表的卡密激活次数，0：无限制
     */
    @NotNull(message = "激活次数不能为空")
    @Min(value = 0, message = "激活次数不能小于 0")
    private Integer equityActiveNum;

    /**
     * 产品描述
     */

    private String description;

    /**
     * 产品状态：0-禁用，1-启用
     */
    @NotNull(message = "产品状态不能为空")
    @Min(value = 0, message = "状态最小值为 0")
    @Max(value = 1, message = "状态最大值为 1")
    private Byte status;

    /**
     * 排序序号
     */
    @NotNull(message = "排序不能为空")
    @Min(value = 0, message = "排序不能小于 0")
    private Integer sortOrder;
}
