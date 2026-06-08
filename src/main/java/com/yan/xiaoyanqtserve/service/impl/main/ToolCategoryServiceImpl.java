package com.yan.xiaoyanqtserve.service.impl.main;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yan.xiaoyanqtserve.common.dto.main.ToolCategoryDto;
import com.yan.xiaoyanqtserve.common.dto.main.ToolCategoryPageRequest;
import com.yan.xiaoyanqtserve.entity.ToolCategory;
import com.yan.xiaoyanqtserve.mapper.ToolCategoryMapper;
import com.yan.xiaoyanqtserve.service.ToolCategoryService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 工具分类表 服务实现类
 *
 * @author yanchen
 * @since 2026-06-06
 */
@Slf4j
@Service
public class ToolCategoryServiceImpl extends ServiceImpl<ToolCategoryMapper, ToolCategory> implements ToolCategoryService {

    @Resource
    private ToolCategoryMapper toolCategoryMapper;

    @Resource
    private ToolCacheManager toolCacheManager;

    /**
     * 分页查询工具分类列表
     *
     * @param pageRequest 分页查询参数
     * @return 分页结果
     */
    @Override
    public Page<ToolCategory> getList(ToolCategoryPageRequest pageRequest) {
        Page<ToolCategory> page = new Page<>(pageRequest.getCurrentPage(), pageRequest.getPageSize());

        LambdaQueryWrapper<ToolCategory> queryWrapper = new LambdaQueryWrapper<>();

        // 添加查询条件
        if (StringUtils.hasText(pageRequest.getName())) {
            queryWrapper.like(ToolCategory::getName, pageRequest.getName());
        }
        if (pageRequest.getStatus() != null) {
            queryWrapper.eq(ToolCategory::getStatus, pageRequest.getStatus());
        }

        // 按排序字段排序
        queryWrapper.orderByAsc(ToolCategory::getSortOrder);

        return this.page(page, queryWrapper);
    }

    /**
     * 根据ID获取工具分类详情
     *
     * @param id 分类ID
     * @return 分类详情
     */
    @Override
    public ToolCategory getDetailById(Long id) {
        return toolCacheManager.getCategoryDetail(id, () -> toolCategoryMapper.selectById(id));
    }

    /**
     * 获取所有启用的分类列表（按排序序号升序）
     *
     * @return 分类列表
     */
    @Override
    public List<ToolCategory> getEnabledCategories() {
        return toolCacheManager.getEnabledCategories(() -> {
            LambdaQueryWrapper<ToolCategory> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ToolCategory::getStatus, 1)
                    .orderByAsc(ToolCategory::getSortOrder);
            return toolCategoryMapper.selectList(queryWrapper);
        });
    }

    /**
     * 新增工具分类
     *
     * @param dto 分类信息
     */
    @Override
    public void add(ToolCategoryDto dto) {
        // 检查分类名称是否已存在
        LambdaQueryWrapper<ToolCategory> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.eq(ToolCategory::getName, dto.getName());
        if (this.count(checkWrapper) > 0) {
            throw new RuntimeException("分类名称「" + dto.getName() + "」已存在");
        }

        ToolCategory toolCategory = new ToolCategory();
        BeanUtils.copyProperties(dto, toolCategory);
        toolCategory.setCreatedAt(LocalDateTime.now());
        toolCategory.setUpdatedAt(LocalDateTime.now());

        boolean saved = this.save(toolCategory);
        log.info("[ToolCategory] 新增分类: name={}, id={}, saved={}", dto.getName(), toolCategory.getId(), saved);

        toolCacheManager.evictCategoryCache(toolCategory.getId());
    }

    /**
     * 修改工具分类
     *
     * @param dto 分类信息
     */
    @Override
    public void update(ToolCategoryDto dto) {
        ToolCategory toolCategory = new ToolCategory();
        BeanUtils.copyProperties(dto, toolCategory);
        toolCategory.setUpdatedAt(LocalDateTime.now());

        this.updateById(toolCategory);

        toolCacheManager.evictCategoryCache(toolCategory.getId());
    }

    /**
     * 根据ID删除工具分类
     *
     * @param id 分类ID
     */
    @Override
    public void deleteById(Long id) {
        toolCategoryMapper.deleteById(id);

        toolCacheManager.evictCategoryCache(id);
    }
}
