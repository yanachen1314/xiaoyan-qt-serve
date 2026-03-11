package com.yan.xiaoyanqtserve.common.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 版本比较结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VersionComparisonResult {
    private boolean latest;         // 是否为最新版
    private boolean updateRequired; // 是否需要更新
    private String updateLevel;     // 更新级别: "major", "minor", "patch", "none", "unknown"
    private String message;         // 提示信息
}