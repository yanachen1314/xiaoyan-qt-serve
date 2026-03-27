package com.yan.xiaoyanqtserve.common.dto.key;

import com.yan.xiaoyanqtserve.common.dto.adminUser.PageRequest;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 密钥信息分页查询请求
 * @author yanchen
 * @since 2026/3/14
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class KeyInfoPageRequest extends PageRequest {
    /**
     * 密钥值（支持模糊查询）
     */
    private String keyValue;

    /**
     * 密钥状态（0-禁用，1-启用）
     */
    @Min(value = 0, message = "该值仅允许0或1")
    @Max(value = 1, message = "该值仅允许0或1")
    private Byte status;

    /**
     * 激活状态（0-未激活，1-已激活）
     */
    @Min(value = 0, message = "该值仅允许 0 或 1")
    @Max(value = 1, message = "该值仅允许 0 或 1")
    private Byte activated;

    /**
     * 销售状态（0-未销售，1-已销售）
     */
    @Min(value = 0, message = "该值仅允许 0 或 1")
    @Max(value = 1, message = "该值仅允许 0 或 1")
    private Byte selled;

    /**
     * 使用场景
     */
    private String usageType;

    /**
     * 产品类型ID
     */
    private Integer productTypeId;
}
