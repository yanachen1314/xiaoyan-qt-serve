package com.yan.xiaoyanqtserve.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yan.xiaoyanqtserve.common.dto.main.ToolItemDto;
import com.yan.xiaoyanqtserve.common.dto.main.ToolItemPageRequest;
import com.yan.xiaoyanqtserve.common.utils.ApiResponse;
import com.yan.xiaoyanqtserve.entity.ToolItem;
import com.yan.xiaoyanqtserve.service.ToolItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 【管理后台】工具项管理
 *
 * @author yanchen
 * @since 2026-06-06
 */
@RestController
@RequestMapping("/api/admin/toolItem")
@RequiredArgsConstructor
public class AdminToolItemController {
    private final ToolItemService toolItemService;

    /**
     * 分页查询工具项列表
     *
     * @param pageRequest 分页查询参数
     * @return 分页结果
     */
    @PostMapping("/list")
    public ApiResponse<Page<ToolItem>> list(@RequestBody ToolItemPageRequest pageRequest) {
        Page<ToolItem> toolItemPage = toolItemService.getList(pageRequest);
        return ApiResponse.success(toolItemPage);
    }

    /**
     * 根据 ID 获取工具项详情
     *
     * @param id 工具项 ID
     * @return 工具项信息
     */
    @GetMapping("/{id}")
    public ApiResponse<ToolItem> getById(@PathVariable Long id) {
        ToolItem toolItem = toolItemService.getDetailById(id);
        if (toolItem == null) {
            return ApiResponse.fail("工具不存在");
        }
        return ApiResponse.success(toolItem);
    }

    /**
     * 新增工具项
     *
     * @param dto 工具项信息
     * @return 操作结果
     */
    @PostMapping
    public ApiResponse<Void> add(@Valid @RequestBody ToolItemDto dto) {
        toolItemService.add(dto);
        return ApiResponse.success();
    }

    /**
     * 修改工具项
     *
     * @param dto 工具项信息
     * @return 操作结果
     */
    @PostMapping("/update")
    public ApiResponse<Void> update(@Valid @RequestBody ToolItemDto dto) {
        toolItemService.update(dto);
        return ApiResponse.success();
    }

    /**
     * 删除工具项
     *
     * @param id 工具项 ID
     * @return 操作结果
     */
    @PostMapping("/delete/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        toolItemService.deleteById(id);
        return ApiResponse.success();
    }
}
