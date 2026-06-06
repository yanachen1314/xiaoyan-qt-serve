package com.yan.xiaoyanqtserve.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yan.xiaoyanqtserve.common.dto.main.ToolCategoryDto;
import com.yan.xiaoyanqtserve.common.dto.main.ToolCategoryPageRequest;
import com.yan.xiaoyanqtserve.entity.ToolCategory;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 工具分类表 服务类
 *
 * @author yanchen
 * @since 2026-06-06
 */
public interface ToolCategoryService extends IService<ToolCategory> {

    /**
     * 分页查询工具分类列表
     *
     * @param pageRequest 分页查询参数
     * @return 分页结果
     */
    Page<ToolCategory> getList(ToolCategoryPageRequest pageRequest);

    /**
     * 根据ID获取工具分类详情
     *
     * @param id 分类ID
     * @return 分类详情
     */
    ToolCategory getDetailById(Long id);

    /**
     * 获取所有启用的分类列表（按排序序号升序）
     *
     * @return 分类列表
     */
    List<ToolCategory> getEnabledCategories();

    /**
     * 新增工具分类
     *
     * @param dto 分类信息
     */
    void add(ToolCategoryDto dto);

    /**
     * 修改工具分类
     *
     * @param dto 分类信息
     */
    void update(ToolCategoryDto dto);

    /**
     * 根据ID删除工具分类
     *
     * @param id 分类ID
     */
    void deleteById(Long id);
}
