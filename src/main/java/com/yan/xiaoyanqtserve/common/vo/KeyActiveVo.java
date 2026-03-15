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
    // 密钥
    private String apiKey;
    // 设备
    private String devices;

    // 激活次数
    private String activationNums;
    // 剩余激活次数
    private String remainingActiveNum;

    // 总使用次数
    private String totalUsage;
    // 剩余使用次数
    private String residualUsage;

    // 过期时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime expireTime;
}
