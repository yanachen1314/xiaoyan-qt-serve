# 工具管理缓存重构设计

## 背景

当前 `ToolItemServiceImpl` 和 `ToolCategoryServiceImpl` 各自维护缓存逻辑，存在以下问题：

1. **`deleteByPattern` 使用 `KEYS` 命令** — 生产环境数据量大时阻塞 Redis
2. **缓存清除过度扩散** — 工具项变更时连带清除分类缓存，反之亦然
3. **幽灵 Key** — `main:tool:list` 在 `clearCache` 中被删除但从未写入
4. **两个 Service 缓存逻辑重复** — 相同的 get/set/clearCache 模式各写一遍
5. **无缓存穿透防护** — 查询不存在的 id 会反复打 DB

## 设计目标

- 彻底抛弃当前两个 Service 中的缓存实现
- 在现有 `RedisUtil` 之上封装 `ToolCacheManager`
- 集中管理所有缓存 Key、过期策略、读取和失效逻辑
- 精确化缓存失效，消除 `deleteByPattern`
- 支持 cache-aside 模式（缓存穿透防护）

## 架构

```
┌─────────────────────────────────────────────────────┐
│                  Controller 层                       │
│  MainToolController / AdminTool*Controller           │
└──────────────────────┬──────────────────────────────┘
                       │ 调用
┌──────────────────────▼──────────────────────────────┐
│                  Service 层                          │
│  ToolItemServiceImpl / ToolCategoryServiceImpl       │
│  （只关心业务逻辑，不直接操作缓存）                      │
└──────────────────────┬──────────────────────────────┘
                       │ 调用
┌──────────────────────▼──────────────────────────────┐
│              ToolCacheManager                        │
│  - 缓存 Key 常量集中定义                               │
│  - cache-aside 读取（Supplier<T> loader）             │
│  - 精确化写失效（按操作类型清除对应 key）                  │
└──────────────────────┬──────────────────────────────┘
                       │ 调用
┌──────────────────────▼──────────────────────────────┐
│              RedisUtil                               │
│  - 底层 Redis 操作工具（保持不变）                       │
└─────────────────────────────────────────────────────┘
```

## 缓存 Key 设计

| Key | 类型 | 过期时间 | 用途 |
|-----|------|---------|------|
| `tool:category:enabled:list` | String (List) | 30min | 所有启用的分类列表 |
| `tool:category:{id}` | String (Object) | 30min | 单个分类详情 |
| `tool:item:enabled:list` | String (List) | 30min | 所有启用的工具项列表 |
| `tool:item:enabled:grouped` | String (Map) | 30min | 按分类分组的工具列表（主站用） |
| `tool:item:{id}` | String (Object) | 30min | 单个工具项详情 |

**命名规则：** `tool:{entity}:{qualifier}`，去掉原来的 `main:` 前缀（工具管理只有一套，无需区分）。

## 缓存失效策略

### 分类变更（add / update / delete）

```
清除：
  - tool:category:enabled:list
  - tool:category:{id}（如果是 update/delete）
  - tool:item:enabled:grouped（分类名变化影响分组结果）
不清除：
  - tool:item:*（工具项本身没变）
  - tool:item:enabled:list（工具项列表没变）
```

### 工具项变更（add / update / delete）

```
清除：
  - tool:item:enabled:list
  - tool:item:enabled:grouped
  - tool:item:{id}（如果是 update/delete）
不清除：
  - tool:category:*（分类本身没变）
```

**关键改进：** 分类和工具项的缓存失效完全解耦，不再互相扩散。

## ToolCacheManager 接口设计

```java
@Component
public class ToolCacheManager {

    private final RedisUtil redisUtil;

    // ==================== 读取（cache-aside）====================

    /**
     * 获取启用的分类列表（缓存穿透防护：空结果也会短暂缓存）
     */
    public List<ToolCategory> getEnabledCategories(Supplier<List<ToolCategory>> loader);

    /**
     * 获取启用的工具项列表
     */
    public List<ToolItem> getEnabledItems(Supplier<List<ToolItem>> loader);

    /**
     * 获取按分类分组的工具列表（主站接口用）
     */
    public Map<String, List<ToolItem>> getGroupedItems(Supplier<Map<String, List<ToolItem>>> loader);

    /**
     * 获取单个工具项详情
     */
    public ToolItem getItemDetail(Long id, Supplier<ToolItem> loader);

    /**
     * 获取单个分类详情
     */
    public ToolCategory getCategoryDetail(Long id, Supplier<ToolCategory> loader);

    // ==================== 写失效 ====================

    /**
     * 分类变更后清除相关缓存
     * @param categoryId 变更的分类 ID（可选，null 表示不清单条详情缓存）
     */
    public void evictCategoryCache(Long categoryId);

    /**
     * 工具项变更后清除相关缓存
     * @param itemId 变更的工具项 ID（可选）
     */
    public void evictItemCache(Long itemId);
}
```

## 读取模式示例

```java
// ToolItemServiceImpl 重构后
public Map<String, List<ToolItem>> getEnabledItemsGroupByCategory() {
    return toolCacheManager.getGroupedItems(() -> {
        // 这个 lambda 只在缓存未命中时执行
        List<ToolCategory> categories = toolCategoryService.getEnabledCategories();
        if (categories == null || categories.isEmpty()) {
            return Collections.emptyMap();
        }
        // ... 查询 DB 并组装结果
        return result;
    });
}
```

```java
// ToolCacheManager 内部
public Map<String, List<ToolItem>> getGroupedItems(Supplier<Map<String, List<ToolItem>>> loader) {
    Object cached = redisUtil.get(KEY_ITEM_GROUPED);
    if (cached instanceof Map<?, ?> map && !map.isEmpty()) {
        return (Map<String, List<ToolItem>>) map;
    }

    Map<String, List<ToolItem>> result = loader.get();
    if (result != null && !result.isEmpty()) {
        redisUtil.set(KEY_ITEM_GROUPED, result, EXPIRE_MINUTES, TimeUnit.MINUTES);
    } else {
        // 空结果缓存 5 分钟，防止缓存穿透
        redisUtil.set(KEY_ITEM_GROUPED, Collections.emptyMap(), 5, TimeUnit.MINUTES);
    }
    return result;
}
```

## 需要修改的文件

| 文件 | 操作 | 说明 |
|------|------|------|
| `ToolCacheManager.java` | **新建** | 集中缓存管理类 |
| `ToolItemServiceImpl.java` | **重构** | 删除缓存常量和缓存逻辑，改为调用 ToolCacheManager |
| `ToolCategoryServiceImpl.java` | **重构** | 同上 |
| `RedisUtil.java` | **小改** | 用 `SCAN` 替代 `KEYS` 的 `deleteByPattern`（可选，暂不改也行） |

**不需要修改的文件：** Controller 层、Mapper 层、Entity 层、前端代码 — 这些不涉及缓存逻辑。

## 不做的事情（YAGNI）

- 不引入 Spring Cache 注解（保持现有 RedisUtil 风格一致）
- 不引入事件驱动（规模不需要）
- 不加分布式锁防击穿（工具列表不是热点 key，过期重建代价很低）
- 不改 RedisUtil 的 `deleteByPattern` 实现（当前工具管理的 key 数量很少，KEYS 不会阻塞；未来可单独优化）
