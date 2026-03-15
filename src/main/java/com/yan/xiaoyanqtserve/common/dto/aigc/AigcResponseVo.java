package com.yan.xiaoyanqtserve.common.dto.aigc;

import lombok.*;

/**
 * AIGC处理结果视图
 * @author yanchen
 * @since 2025/7/27 21:52
 */
@Data
@Builder
public class AigcResponseVo {

    /**
     * 降后的文本
     */
    private  String dealResult;

    /**
     * 输入字符数
     */
    private Integer inputChars;
}
