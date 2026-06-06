package com.yan.xiaoyanqtserve.controller.main;

import com.yan.xiaoyanqtserve.common.annotaions.RateLimiter;
import com.yan.xiaoyanqtserve.common.dto.main.ToolListResponse;
import com.yan.xiaoyanqtserve.common.enums.RateLimiterType;
import com.yan.xiaoyanqtserve.common.utils.ApiResponse;
import com.yan.xiaoyanqtserve.entity.ToolItem;
import com.yan.xiaoyanqtserve.service.ToolItemService;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 【主站】工具接口
 * 提供工具列表和工具详情等公开接口
 *
 * @author yanchen
 * @since 2026-06-06
 */
@Slf4j
@RestController
@RequestMapping("/api/main/tools")
@RequiredArgsConstructor
public class MainToolController {

    private final ToolItemService toolItemService;

    /**
     * 获取工具列表（按分类分组）
     *
     * @return 工具列表
     */
    @RateLimiter(key = "mainToolsList", time = 60, count = 30, limitBy = RateLimiterType.DEVICE_ID)
    @GetMapping
    public ApiResponse<List<ToolListResponse>> getToolList() {
        try {
            // 获取按分类分组的工具列表
            Map<String, List<ToolItem>> groupedItems = toolItemService.getEnabledItemsGroupByCategory();

            // 转换为响应格式
            List<ToolListResponse> response = new ArrayList<>();
            for (Map.Entry<String, List<ToolItem>> entry : groupedItems.entrySet()) {
                String categoryName = entry.getKey();
                List<ToolItem> items = entry.getValue();

                List<ToolListResponse.ToolItemVo> toolItemVos = items.stream()
                        .map(this::convertToVo)
                        .toList();

                response.add(ToolListResponse.builder()
                        .categoryName(categoryName)
                        .tools(toolItemVos)
                        .build());
            }

            return ApiResponse.success(response);
        } catch (Exception e) {
            log.error("获取工具列表失败", e);
            return ApiResponse.fail("获取工具列表失败");
        }
    }

    /**
     * 获取工具详情
     *
     * @param id 工具ID
     * @return 工具详情
     */
    @RateLimiter(key = "mainToolDetail", time = 60, count = 60, limitBy = RateLimiterType.DEVICE_ID)
    @GetMapping("/{id}")
    public ApiResponse<ToolListResponse.ToolItemVo> getToolDetail(@PathVariable Long id) {
        try {
            ToolItem toolItem = toolItemService.getDetailById(id);
            if (toolItem == null) {
                return ApiResponse.fail("工具不存在");
            }

            // 增加点击次数
            toolItemService.incrementClickCount(id);

            return ApiResponse.success(convertToVo(toolItem));
        } catch (Exception e) {
            log.error("获取工具详情失败, id={}", id, e);
            return ApiResponse.fail("获取工具详情失败");
        }
    }

    /**
     * 将 ToolItem 转换为 ToolItemVo
     *
     * @param toolItem 工具项实体
     * @return 工具项VO
     */
    private ToolListResponse.ToolItemVo convertToVo(ToolItem toolItem) {
        return ToolListResponse.ToolItemVo.builder()
                .id(toolItem.getId())
                .name(toolItem.getName())
                .desc(toolItem.getDescription())
                .url(toolItem.getUrl())
                .icon(toolItem.getIcon())
                .hot(toolItem.getHot() != null && toolItem.getHot() == 1)
                .official(toolItem.getOfficial() != null && toolItem.getOfficial() == 1)
                .build();
    }
}
