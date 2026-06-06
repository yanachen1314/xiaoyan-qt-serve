package com.yan.xiaoyanqtserve.common.dto.main;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 工具列表响应 DTO
 *
 * @author yanchen
 * @since 2026-06-06
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ToolListResponse {
    /**
     * 分类名称
     */
    private String categoryName;

    /**
     * 工具列表
     */
    private List<ToolItemVo> tools;

    /**
     * 工具项 VO
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ToolItemVo {
        /**
         * 工具ID
         */
        private Long id;

        /**
         * 工具名称
         */
        private String name;

        /**
         * 工具描述
         */
        private String desc;

        /**
         * 链接地址
         */
        private String url;

        /**
         * 图标
         */
        private String icon;

        /**
         * 是否热门
         */
        private Boolean hot;

        /**
         * 是否官方
         */
        private Boolean official;
    }
}
