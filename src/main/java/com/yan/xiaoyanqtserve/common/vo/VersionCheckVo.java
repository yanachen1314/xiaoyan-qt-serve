package com.yan.xiaoyanqtserve.common.vo;

import lombok.Builder;
import lombok.Data;

/**
 * 版本检查结果
 */
@Data
@Builder
public class VersionCheckVo {
    /**
     * 服务器最新版本
     */
    private String version;
    /**
     * 客户端传入版本（用于调试）
     */
    private String clientVersion;
    /**
     * 是否最新
     */
    private Boolean latest;
    /**
     * 是否需要更新
     */
    private Boolean updateRequired;

    /**
     * 提示语
     */
    private String message;
    /**
     * 更新链接
     */
    private String linkUrl;
}