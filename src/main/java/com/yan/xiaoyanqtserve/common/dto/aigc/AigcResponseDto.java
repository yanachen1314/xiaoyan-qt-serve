package com.yan.xiaoyanqtserve.common.dto.aigc;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * AIGC处理结果封装
 * @author yanchen
 * @since 2025/7/27 21:52
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class AigcResponseDto {

    /**
     * 降后的文本或文档下载路径
     */
    private  String reducedContent;

    /**
     * 输入字符数
     */
    private Integer inputChars;
}
