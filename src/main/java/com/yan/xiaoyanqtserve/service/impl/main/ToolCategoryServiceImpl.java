package com.yan.xiaoyanqtserve.service.impl.main;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yan.xiaoyanqtserve.common.dto.main.ToolCategoryDto;
import com.yan.xiaoyanqtserve.common.dto.main.ToolCategoryPageRequest;
import com.yan.xiaoyanqtserve.common.utils.RedisUtil;
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
import java.util.concurrent.TimeUnit;

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
    private RedisUtil redisUtil;

    /**
     * Redis缓存Key前缀
     */
    private static final String CACHE_KEY_PREFIX = "main:tool:category:";

    /**
     * Redis缓存Key - 所有启用的分类列表
     */
    private static final String CACHE_KEY_ENABLED_LIST = "main:tool:category:enabled:list";

    /**
     * 缓存过期时间（分钟）
     */
    private static final int CACHE_EXPIRE_MINUTES = 30;

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
        String cacheKey = CACHE_KEY_PREFIX + id;

        // 从缓存获取
        if (redisUtil.hasKey(cacheKey)) {
            return (ToolCategory) redisUtil.get(cacheKey);
        }

        // 从数据库获取
        ToolCategory toolCategory = toolCategoryMapper.selectById(id);
        if (toolCategory != null) {
            // 存入缓存
            redisUtil.set(cacheKey, toolCategory, CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES);
        }

        return toolCategory;
    }

    /**
     * 获取所有启用的分类列表（按排序序号升序）
     *
     * @return 分类列表
     */
    @Override
    public List<ToolCategory> getEnabledCategories() {
        // 从缓存获取
        if (redisUtil.hasKey(CACHE_KEY_ENABLED_LIST)) {
            return (List<ToolCategory>) redisUtil.get(CACHE_KEY_ENABLED_LIST);
        }

        // 从数据库获取
        LambdaQueryWrapper<ToolCategory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ToolCategory::getStatus, 1)
                .orderByAsc(ToolCategory::getSortOrder);

        List<ToolCategory> categories = toolCategoryMapper.selectList(queryWrapper);

        // 存入缓存
        if (categories != null && !categories.isEmpty()) {
            redisUtil.set(CACHE_KEY_ENABLED_LIST, categories, CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES);
        }

        return categories;
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

        this.save(toolCategory);

        // 清除缓存
        clearCache();
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

        // 清除缓存
        clearCache();
    }

    /**
     * 根据ID删除工具分类
     *
     * @param id 分类ID
     */
    @Override
    public void deleteById(Long id) {
        toolCategoryMapper.deleteById(id);

        // 清除缓存
        clearCache();
    }

    /**
     * 清除缓存
     */
    private void clearCache() {
        // 删除分类缓存
        redisUtil.delete(CACHE_KEY_ENABLED_LIST);

        // 同步清除主站工具列表缓存（分类变化会影响主站展示）
        redisUtil.delete("main:tool:list");

        log.debug("清除工具分类缓存");
    }
}
