package com.yan.xiaoyanqtserve.common.dto.aigc;

import lombok.Data;

/**
 * 模型处理结果封装
 * @author yanchen
 * @since 2025/8/12 12:47
 */
@Data
public class AigcModelResultDto {
    // 输入文本
    private String inputText;

    // 输出文本
    private String outputText;

    // 模型输入的token数
    private Integer inputTokens;
    // 模型输出的token数
    private Integer outputTokens;
    // 模型输入输出的token数之和
    private Integer totalTokens;
}
