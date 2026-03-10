package com.yan.xiaoyanqtserve.common.dto.adminUser;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 分页基础参数
 */
@Data
public class PageRequest {
    /**
     * 当前页码，必须 ≥ 1
     */
    @Min(value = 1, message = "当前页码必须大于等于1")
    @NotNull(message = "当前页码不能为空")
    private int currentPage = 1;

    /**
     * 每页显示的记录数，区间范围 [1,100]
     */
    @Min(value = 1, message = "每页记录数必须大于等于1")
    @Max(value = 100, message = "每页记录数不能超过100")
    @NotNull(message = "每页记录数不能为空")
    private int pageSize = 10;

}