package com.yan.xiaoyanqtserve.common.dto.key;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 密钥激活数据传输对象
 * @author yanchen
 * @since 2026/3/10 20:18
 */
@Data
public class KeyActiveDto {

    /**
     * 设备ID有两种来源
     * 1. 客户端设备ID是UUID生成的。
     * 2. WEB端设备ID是用户浏览器设备指纹生成的。
     */
    @NotBlank(message = "设备ID不能为空")
    private String deviceID;
}
