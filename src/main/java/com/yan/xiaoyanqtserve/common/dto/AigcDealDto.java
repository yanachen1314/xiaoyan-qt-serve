package com.yan.xiaoyanqtserve.common.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 段落AI处理请求参数
 * @author yanchen
 * @since 2026/3/10 22:40
 */
@Data
public class AigcDealDto {

    /**
     * 原始文本
     */
    @NotBlank(message = "原始文本不能为空")
    private String originalText;
}
