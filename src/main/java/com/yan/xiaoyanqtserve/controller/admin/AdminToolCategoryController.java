package com.yan.xiaoyanqtserve.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yan.xiaoyanqtserve.common.dto.main.ToolCategoryDto;
import com.yan.xiaoyanqtserve.common.dto.main.ToolCategoryPageRequest;
import com.yan.xiaoyanqtserve.common.utils.ApiResponse;
import com.yan.xiaoyanqtserve.entity.ToolCategory;
import com.yan.xiaoyanqtserve.service.ToolCategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 【管理后台】工具分类管理
 *
 * @author yanchen
 * @since 2026-06-06
 */
@RestController
@RequestMapping("/api/admin/toolCategory")
@RequiredArgsConstructor
public class AdminToolCategoryController {
    private final ToolCategoryService toolCategoryService;

    /**
     * 分页查询工具分类列表
     *
     * @param pageRequest 分页查询参数
     * @return 分页结果
     */
    @PostMapping("/list")
    public ApiResponse<Page<ToolCategory>> list(@RequestBody ToolCategoryPageRequest pageRequest) {
        Page<ToolCategory> toolCategoryPage = toolCategoryService.getList(pageRequest);
        return ApiResponse.success(toolCategoryPage);
    }

    /**
     * 根据 ID 获取工具分类详情
     *
     * @param id 分类 ID
     * @return 分类信息
     */
    @GetMapping("/{id}")
    public ApiResponse<ToolCategory> getById(@PathVariable Long id) {
        ToolCategory toolCategory = toolCategoryService.getDetailById(id);
        if (toolCategory == null) {
            return ApiResponse.fail("分类不存在");
        }
        return ApiResponse.success(toolCategory);
    }

    /**
     * 新增工具分类
     *
     * @param dto 分类信息
     * @return 操作结果
     */
    @PostMapping
    public ApiResponse<Void> add(@Valid @RequestBody ToolCategoryDto dto) {
        toolCategoryService.add(dto);
        return ApiResponse.success();
    }

    /**
     * 修改工具分类
     *
     * @param dto 分类信息
     * @return 操作结果
     */
    @PostMapping("/update")
    public ApiResponse<Void> update(@Valid @RequestBody ToolCategoryDto dto) {
        toolCategoryService.update(dto);
        return ApiResponse.success();
    }

    /**
     * 删除工具分类
     *
     * @param id 分类 ID
     * @return 操作结果
     */
    @PostMapping("/delete/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        toolCategoryService.deleteById(id);
        return ApiResponse.success();
    }
}
