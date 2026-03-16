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

    @NotBlank(message = "设备ID不能为空")
    private String deviceID;
}
