package com.yan.xiaoyanqtserve.service.impl.main;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yan.xiaoyanqtserve.common.dto.main.ToolItemDto;
import com.yan.xiaoyanqtserve.common.dto.main.ToolItemPageRequest;
import com.yan.xiaoyanqtserve.common.utils.RedisUtil;
import com.yan.xiaoyanqtserve.entity.ToolCategory;
import com.yan.xiaoyanqtserve.entity.ToolItem;
import com.yan.xiaoyanqtserve.mapper.ToolItemMapper;
import com.yan.xiaoyanqtserve.service.ToolCategoryService;
import com.yan.xiaoyanqtserve.service.ToolItemService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 工具项表 服务实现类
 *
 * @author yanchen
 * @since 2026-06-06
 */
@Slf4j
@Service
public class ToolItemServiceImpl extends ServiceImpl<ToolItemMapper, ToolItem> implements ToolItemService {

    @Resource
    private ToolItemMapper toolItemMapper;

    @Resource
    private RedisUtil redisUtil;

    @Resource
    private ToolCategoryService toolCategoryService;

    /**
     * Redis缓存Key前缀
     */
    private static final String CACHE_KEY_PREFIX = "main:tool:item:";

    /**
     * Redis缓存Key - 所有启用的工具项列表
     */
    private static final String CACHE_KEY_ENABLED_LIST = "main:tool:item:enabled:list";

    /**
     * Redis缓存Key - 工具列表（按分类分组）
     */
    private static final String CACHE_KEY_GROUP_BY_CATEGORY = "main:tool:list";

    /**
     * 缓存过期时间（分钟）
     */
    private static final int CACHE_EXPIRE_MINUTES = 30;

    /**
     * 分页查询工具项列表
     *
     * @param pageRequest 分页查询参数
     * @return 分页结果
     */
    @Override
    public Page<ToolItem> getList(ToolItemPageRequest pageRequest) {
        Page<ToolItem> page = new Page<>(pageRequest.getCurrentPage(), pageRequest.getPageSize());

        LambdaQueryWrapper<ToolItem> queryWrapper = new LambdaQueryWrapper<>();

        // 添加查询条件
        if (pageRequest.getCategoryId() != null) {
            queryWrapper.eq(ToolItem::getCategoryId, pageRequest.getCategoryId());
        }
        if (StringUtils.hasText(pageRequest.getName())) {
            queryWrapper.like(ToolItem::getName, pageRequest.getName());
        }
        if (pageRequest.getStatus() != null) {
            queryWrapper.eq(ToolItem::getStatus, pageRequest.getStatus());
        }
        if (pageRequest.getHot() != null) {
            queryWrapper.eq(ToolItem::getHot, pageRequest.getHot());
        }
        if (pageRequest.getOfficial() != null) {
            queryWrapper.eq(ToolItem::getOfficial, pageRequest.getOfficial());
        }

        // 按排序字段排序
        queryWrapper.orderByAsc(ToolItem::getSortOrder);

        return this.page(page, queryWrapper);
    }

    /**
     * 根据ID获取工具项详情
     *
     * @param id 工具项ID
     * @return 工具项详情
     */
    @Override
    public ToolItem getDetailById(Long id) {
        String cacheKey = CACHE_KEY_PREFIX + id;

        // 从缓存获取
        Object cached = redisUtil.get(cacheKey);
        if (cached instanceof ToolItem item) {
            return item;
        }

        // 从数据库获取
        ToolItem toolItem = toolItemMapper.selectById(id);
        if (toolItem != null) {
            redisUtil.set(cacheKey, toolItem, CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES);
        }

        return toolItem;
    }

    /**
     * 根据分类ID获取工具项列表
     *
     * @param categoryId 分类ID
     * @return 工具项列表
     */
    @Override
    public List<ToolItem> getItemsByCategoryId(Long categoryId) {
        String cacheKey = CACHE_KEY_PREFIX + "category:" + categoryId;

        // 从缓存获取
        Object cached = redisUtil.get(cacheKey);
        if (cached instanceof List<?> list && !list.isEmpty()) {
            @SuppressWarnings("unchecked")
            List<ToolItem> result = (List<ToolItem>) list;
            return result;
        }

        // 从数据库获取
        LambdaQueryWrapper<ToolItem> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ToolItem::getCategoryId, categoryId)
                .eq(ToolItem::getStatus, 1)
                .orderByAsc(ToolItem::getSortOrder);

        List<ToolItem> items = toolItemMapper.selectList(queryWrapper);

        if (items != null && !items.isEmpty()) {
            redisUtil.set(cacheKey, items, CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES);
        }

        return items;
    }

    /**
     * 获取所有启用的工具项，按分类分组
     *
     * @return 分类和工具项的映射
     */
    @Override
    @SuppressWarnings("unchecked")
    public Map<String, List<ToolItem>> getEnabledItemsGroupByCategory() {
        // 从缓存获取
        Object cached = redisUtil.get(CACHE_KEY_GROUP_BY_CATEGORY);
        if (cached instanceof Map<?, ?> map && !map.isEmpty()) {
            return (Map<String, List<ToolItem>>) map;
        }

        // 获取所有启用的分类
        List<ToolCategory> categories = toolCategoryService.getEnabledCategories();
        if (categories == null || categories.isEmpty()) {
            return Collections.emptyMap();
        }

        // 获取所有启用的工具项
        LambdaQueryWrapper<ToolItem> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ToolItem::getStatus, 1)
                .orderByAsc(ToolItem::getSortOrder);

        List<ToolItem> allItems = toolItemMapper.selectList(queryWrapper);
        if (allItems == null) {
            allItems = Collections.emptyList();
        }

        // 按分类ID分组
        Map<Long, List<ToolItem>> itemsByCategoryId = allItems.stream()
                .collect(Collectors.groupingBy(ToolItem::getCategoryId));

        // 构建结果：分类名称 -> 工具项列表
        Map<String, List<ToolItem>> result = new LinkedHashMap<>();
        for (ToolCategory category : categories) {
            List<ToolItem> items = itemsByCategoryId.getOrDefault(category.getId(), Collections.emptyList());
            if (!items.isEmpty()) {
                result.put(category.getName(), items);
            }
        }

        // 存入缓存
        if (!result.isEmpty()) {
            redisUtil.set(CACHE_KEY_GROUP_BY_CATEGORY, result, CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES);
        }

        return result;
    }

    /**
     * 新增工具项
     *
     * @param dto 工具项信息
     */
    @Override
    public void add(ToolItemDto dto) {
        ToolItem toolItem = new ToolItem();
        BeanUtils.copyProperties(dto, toolItem);
        toolItem.setClickCount(0L);
        toolItem.setCreatedAt(LocalDateTime.now());
        toolItem.setUpdatedAt(LocalDateTime.now());

        this.save(toolItem);

        // 清除缓存
        clearCache();
    }

    /**
     * 修改工具项
     *
     * @param dto 工具项信息
     */
    @Override
    public void update(ToolItemDto dto) {
        ToolItem toolItem = new ToolItem();
        BeanUtils.copyProperties(dto, toolItem);
        toolItem.setUpdatedAt(LocalDateTime.now());

        this.updateById(toolItem);

        // 清除缓存
        clearCache();
    }

    /**
     * 根据ID删除工具项
     *
     * @param id 工具项ID
     */
    @Override
    public void deleteById(Long id) {
        toolItemMapper.deleteById(id);

        // 清除缓存
        clearCache();
    }

    /**
     * 增加工具项点击次数
     *
     * @param id 工具项ID
     */
    @Override
    public void incrementClickCount(Long id) {
        // 使用 Redis 原子操作增加点击次数
        String clickCountKey = "main:tool:item:click:" + id;
        Long newCount = redisUtil.incr(clickCountKey, 1L);

        // 每10次同步到数据库
        if (newCount % 10 == 0) {
            ToolItem toolItem = new ToolItem();
            toolItem.setId(id);
            toolItem.setClickCount(newCount);
            toolItem.setUpdatedAt(LocalDateTime.now());
            this.updateById(toolItem);
        }
    }

    /**
     * 清除缓存
     */
    private void clearCache() {
        // 删除所有工具项详情缓存（main:tool:item:{id}、main:tool:item:category:{categoryId}）
        redisUtil.deleteByPattern(CACHE_KEY_PREFIX + "*");
        // 删除主站按分类分组的工具列表缓存
        redisUtil.delete(CACHE_KEY_GROUP_BY_CATEGORY);
        // 删除启用工具列表缓存
        redisUtil.delete(CACHE_KEY_ENABLED_LIST);
        // 删除分类相关缓存（分类变化会影响工具项展示）
        redisUtil.deleteByPattern("main:tool:category:*");

        log.debug("清除工具项缓存完成");
    }
}
