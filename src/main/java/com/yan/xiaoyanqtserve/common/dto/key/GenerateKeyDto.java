package com.yan.xiaoyanqtserve.common.dto.key;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 密钥生成请求参数
 * @author yanchen
 * @since 2026/3/10 15:42
 */
@Data
public class GenerateKeyDto {

    /**
     * 密钥状态: 0-禁用, 1-启用
     */
    private Byte status = 1;

    /**
     * 总使用次数(降AI)，0表示无限制
     */
    @NotNull(message = "总使用次数不能为空")
    @Min(value = 0, message = "总使用次数不能小于0")
    private Integer totalUses;

    /**
     * 产品类型或套餐标识，例如: "ai_rewrite_vip", "ai_rewrite_basic"
     * @see com.yan.xiaoyanqtserve.common.enums.ProductType
     */
    @NotNull(message = "密钥产品类型不能为空")
    private String productType;

    /**
     * 激活次数，0-无次数限制
     */
    @NotNull(message = "激活次数不能为空")
    @Min(value = 0, message = "激活次数不能小于0")
    private Integer activationsNum;

    /**
     * 备注信息
     */
    private String remark;
}
