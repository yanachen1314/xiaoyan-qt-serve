package com.yan.xiaoyanqtserve.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yan.xiaoyanqtserve.common.dto.main.ToolItemDto;
import com.yan.xiaoyanqtserve.common.dto.main.ToolItemPageRequest;
import com.yan.xiaoyanqtserve.entity.ToolItem;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * 工具项表 服务类
 *
 * @author yanchen
 * @since 2026-06-06
 */
public interface ToolItemService extends IService<ToolItem> {

    /**
     * 分页查询工具项列表
     *
     * @param pageRequest 分页查询参数
     * @return 分页结果
     */
    Page<ToolItem> getList(ToolItemPageRequest pageRequest);

    /**
     * 根据ID获取工具项详情
     *
     * @param id 工具项ID
     * @return 工具项详情
     */
    ToolItem getDetailById(Long id);

    /**
     * 根据分类ID获取工具项列表
     *
     * @param categoryId 分类ID
     * @return 工具项列表
     */
    List<ToolItem> getItemsByCategoryId(Long categoryId);

    /**
     * 获取所有启用的工具项，按分类分组
     *
     * @return 分类和工具项的映射
     */
    Map<String, List<ToolItem>> getEnabledItemsGroupByCategory();

    /**
     * 新增工具项
     *
     * @param dto 工具项信息
     */
    void add(ToolItemDto dto);

    /**
     * 修改工具项
     *
     * @param dto 工具项信息
     */
    void update(ToolItemDto dto);

    /**
     * 根据ID删除工具项
     *
     * @param id 工具项ID
     */
    void deleteById(Long id);

    /**
     * 增加工具项点击次数
     *
     * @param id 工具项ID
     */
    void incrementClickCount(Long id);
}
