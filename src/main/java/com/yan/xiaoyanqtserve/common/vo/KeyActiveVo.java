package com.yan.xiaoyanqtserve.common.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 密钥激活成功的返回数据
 * @author yanchen
 * @since 2026/3/11 0:15
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class KeyActiveVo {
    private String apiKey;
    private String device;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime expireTime;
}
