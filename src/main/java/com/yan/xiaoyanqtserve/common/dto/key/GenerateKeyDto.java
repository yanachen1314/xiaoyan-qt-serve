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
     * 密钥数量
     */
    @NotNull(message = "密钥数量不能为空")
    @Min(value = 1, message = "密钥数量不能小于1")
    private Integer keyNumber;

    /**
     * 产品类型或套餐标识
     */
    @NotNull(message = "密钥产品类型不能为空")
    private Integer productTypeId;

    /**
     * 备注信息
     */
    private String remark;
}
