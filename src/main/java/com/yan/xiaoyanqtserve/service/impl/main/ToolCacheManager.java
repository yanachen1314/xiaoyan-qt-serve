package com.yan.xiaoyanqtserve.service.impl.main;

import com.yan.xiaoyanqtserve.common.utils.RedisUtil;
import com.yan.xiaoyanqtserve.entity.ToolCategory;
import com.yan.xiaoyanqtserve.entity.ToolItem;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 工具管理缓存管理器
 * 集中管理所有工具相关的缓存 Key、读取和失效逻辑
 *
 * @author yanchen
 * @since 2026-06-06
 */
@Slf4j
@Component
public class ToolCacheManager {

    @Resource
    private RedisUtil redisUtil;

    // ==================== 缓存 Key 常量 ====================

    /** 启用的分类列表 */
    static final String KEY_CATEGORY_ENABLED_LIST = "tool:category:enabled:list";
    /** 单个分类详情前缀 */
    static final String KEY_CATEGORY_PREFIX = "tool:category:";
    /** 启用的工具项列表 */
    static final String KEY_ITEM_ENABLED_LIST = "tool:item:enabled:list";
    /** 按分类分组的工具列表（主站用） */
    static final String KEY_ITEM_GROUPED = "tool:item:enabled:grouped";
    /** 单个工具项详情前缀 */
    static final String KEY_ITEM_PREFIX = "tool:item:";

    // ==================== 过期时间常量 ====================

    /** 正常缓存过期时间（分钟） */
    static final int EXPIRE_MINUTES = 30;
    /** 空结果缓存过期时间（分钟），防止缓存穿透 */
    static final int EMPTY_EXPIRE_MINUTES = 5;

    // ==================== cache-aside 读取方法 ====================

    /**
     * 获取启用的分类列表
     *
     * @param loader 缓存未命中时的数据加载器
     * @return 分类列表
     */
    @SuppressWarnings("unchecked")
    public List<ToolCategory> getEnabledCategories(Supplier<List<ToolCategory>> loader) {
        Object cached = redisUtil.get(KEY_CATEGORY_ENABLED_LIST);
        if (cached instanceof List<?> list && !list.isEmpty()) {
            return (List<ToolCategory>) list;
        }

        List<ToolCategory> result = loader.get();
        if (result != null && !result.isEmpty()) {
            redisUtil.set(KEY_CATEGORY_ENABLED_LIST, result, EXPIRE_MINUTES, TimeUnit.MINUTES);
        } else {
            redisUtil.set(KEY_CATEGORY_ENABLED_LIST, Collections.emptyList(), EMPTY_EXPIRE_MINUTES, TimeUnit.MINUTES);
        }
        return result;
    }

    /**
     * 获取单个分类详情
     *
     * @param id     分类 ID
     * @param loader 缓存未命中时的数据加载器
     * @return 分类详情，可能为 null
     */
    public ToolCategory getCategoryDetail(Long id, Supplier<ToolCategory> loader) {
        String cacheKey = KEY_CATEGORY_PREFIX + id;

        Object cached = redisUtil.get(cacheKey);
        if (cached instanceof ToolCategory category) {
            return category;
        }

        ToolCategory result = loader.get();
        if (result != null) {
            redisUtil.set(cacheKey, result, EXPIRE_MINUTES, TimeUnit.MINUTES);
        } else {
            // 空结果缓存，防止缓存穿透
            redisUtil.set(cacheKey, "NULL", EMPTY_EXPIRE_MINUTES, TimeUnit.MINUTES);
        }
        return result;
    }

    /**
     * 获取启用的工具项列表
     *
     * @param loader 缓存未命中时的数据加载器
     * @return 工具项列表
     */
    @SuppressWarnings("unchecked")
    public List<ToolItem> getEnabledItems(Supplier<List<ToolItem>> loader) {
        Object cached = redisUtil.get(KEY_ITEM_ENABLED_LIST);
        if (cached instanceof List<?> list && !list.isEmpty()) {
            return (List<ToolItem>) list;
        }

        List<ToolItem> result = loader.get();
        if (result != null && !result.isEmpty()) {
            redisUtil.set(KEY_ITEM_ENABLED_LIST, result, EXPIRE_MINUTES, TimeUnit.MINUTES);
        } else {
            redisUtil.set(KEY_ITEM_ENABLED_LIST, Collections.emptyList(), EMPTY_EXPIRE_MINUTES, TimeUnit.MINUTES);
        }
        return result;
    }

    /**
     * 获取按分类分组的工具列表（主站接口用）
     *
     * @param loader 缓存未命中时的数据加载器
     * @return 分类名称 -> 工具项列表 的映射
     */
    @SuppressWarnings("unchecked")
    public Map<String, List<ToolItem>> getGroupedItems(Supplier<Map<String, List<ToolItem>>> loader) {
        Object cached = redisUtil.get(KEY_ITEM_GROUPED);
        if (cached instanceof Map<?, ?> map && !map.isEmpty()) {
            return (Map<String, List<ToolItem>>) map;
        }

        Map<String, List<ToolItem>> result = loader.get();
        if (result != null && !result.isEmpty()) {
            redisUtil.set(KEY_ITEM_GROUPED, result, EXPIRE_MINUTES, TimeUnit.MINUTES);
        } else {
            redisUtil.set(KEY_ITEM_GROUPED, Collections.emptyMap(), EMPTY_EXPIRE_MINUTES, TimeUnit.MINUTES);
        }
        return result;
    }

    /**
     * 获取单个工具项详情
     *
     * @param id     工具项 ID
     * @param loader 缓存未命中时的数据加载器
     * @return 工具项详情，可能为 null
     */
    public ToolItem getItemDetail(Long id, Supplier<ToolItem> loader) {
        String cacheKey = KEY_ITEM_PREFIX + id;

        Object cached = redisUtil.get(cacheKey);
        if (cached instanceof ToolItem item) {
            return item;
        }

        ToolItem result = loader.get();
        if (result != null) {
            redisUtil.set(cacheKey, result, EXPIRE_MINUTES, TimeUnit.MINUTES);
        } else {
            // 空结果缓存，防止缓存穿透
            redisUtil.set(cacheKey, "NULL", EMPTY_EXPIRE_MINUTES, TimeUnit.MINUTES);
        }
        return result;
    }

    // ==================== 写失效方法 ====================

    /**
     * 分类变更后清除相关缓存
     * <p>
     * 清除范围：分类启用列表 + 分类详情 + 按分类分组的工具列表
     * 不清除：工具项相关缓存（工具项本身没变）
     *
     * @param categoryId 变更的分类 ID（null 表示不清单条详情缓存）
     */
    public void evictCategoryCache(Long categoryId) {
        redisUtil.delete(KEY_CATEGORY_ENABLED_LIST);
        redisUtil.delete(KEY_ITEM_GROUPED);
        if (categoryId != null) {
            redisUtil.delete(KEY_CATEGORY_PREFIX + categoryId);
        }
        log.debug("[Cache] 分类缓存已清除: categoryId={}", categoryId);
    }

    /**
     * 工具项变更后清除相关缓存
     * <p>
     * 清除范围：工具项启用列表 + 按分类分组的工具列表 + 工具项详情
     * 不清除：分类相关缓存（分类本身没变）
     *
     * @param itemId 变更的工具项 ID（null 表示不清单条详情缓存）
     */
    public void evictItemCache(Long itemId) {
        redisUtil.delete(KEY_ITEM_ENABLED_LIST);
        redisUtil.delete(KEY_ITEM_GROUPED);
        if (itemId != null) {
            redisUtil.delete(KEY_ITEM_PREFIX + itemId);
        }
        log.debug("[Cache] 工具项缓存已清除: itemId={}", itemId);
    }
}
