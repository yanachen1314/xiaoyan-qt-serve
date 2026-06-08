# 工具管理缓存重构 — 实现计划

> 设计文档：[2026-06-06-tool-cache-refactor-design.md](./2026-06-06-tool-cache-refactor-design.md)

## 任务概览

| # | 任务 | 预估时间 | 依赖 |
|---|------|---------|------|
| 1 | 新建 ToolCacheManager — 缓存 Key 常量 + cache-aside 读取方法 | 5min | 无 |
| 2 | 新建 ToolCacheManager — 写失效方法 | 3min | Task 1 |
| 3 | 重构 ToolCategoryServiceImpl — 移除缓存逻辑，接入 ToolCacheManager | 5min | Task 2 |
| 4 | 重构 ToolItemServiceImpl — 移除缓存逻辑，接入 ToolCacheManager | 5min | Task 2 |
| 5 | 验证 — 编译检查 + 逻辑 Review | 3min | Task 3, 4 |

---

## Task 1: 新建 ToolCacheManager — 缓存 Key 常量 + cache-aside 读取方法

**文件：** `xiaoyanQt_serve/src/main/java/com/yan/xiaoyanqtserve/service/impl/main/ToolCacheManager.java`

**做什么：**

1. 创建 `ToolCacheManager` 类，标注 `@Component`
2. 注入 `RedisUtil`
3. 定义所有缓存 Key 常量和过期时间常量：
   ```java
   // Key 常量
   static final String KEY_CATEGORY_ENABLED_LIST = "tool:category:enabled:list";
   static final String KEY_CATEGORY_PREFIX = "tool:category:";
   static final String KEY_ITEM_ENABLED_LIST = "tool:item:enabled:list";
   static final String KEY_ITEM_GROUPED = "tool:item:enabled:grouped";
   static final String KEY_ITEM_PREFIX = "tool:item:";

   // 过期时间
   static final int EXPIRE_MINUTES = 30;
   static final int EMPTY_EXPIRE_MINUTES = 5;  // 空结果缓存时间，防穿透
   ```

4. 实现 cache-aside 读取方法（每个方法内部逻辑：get → 命中返回 → 未命中调用 loader → 写缓存 → 返回）：
   - `getEnabledCategories(Supplier<List<ToolCategory>> loader)`
   - `getCategoryDetail(Long id, Supplier<ToolCategory> loader)`
   - `getEnabledItems(Supplier<List<ToolItem>> loader)`
   - `getGroupedItems(Supplier<Map<String, List<ToolItem>>> loader)`
   - `getItemDetail(Long id, Supplier<ToolItem> loader)`

5. 空结果缓存 5 分钟（`EMPTY_EXPIRE_MINUTES`），防止缓存穿透

**不做什么：** 不修改任何现有文件

---

## Task 2: 新建 ToolCacheManager — 写失效方法

**文件：** 同 Task 1

**做什么：**

1. 实现精确化写失效方法：
   ```java
   /**
    * 分类变更后清除相关缓存
    * @param categoryId 变更的分类 ID（null 表示不清单条详情）
    */
   public void evictCategoryCache(Long categoryId) {
       redisUtil.delete(KEY_CATEGORY_ENABLED_LIST);
       redisUtil.delete(KEY_ITEM_GROUPED);  // 分类名影响分组结果
       if (categoryId != null) {
           redisUtil.delete(KEY_CATEGORY_PREFIX + categoryId);
       }
   }

   /**
    * 工具项变更后清除相关缓存
    * @param itemId 变更的工具项 ID（null 表示不清单条详情）
    */
   public void evictItemCache(Long itemId) {
       redisUtil.delete(KEY_ITEM_ENABLED_LIST);
       redisUtil.delete(KEY_ITEM_GROUPED);
       if (itemId != null) {
           redisUtil.delete(KEY_ITEM_PREFIX + itemId);
       }
   }
   ```

**不做什么：** 不使用 `deleteByPattern`，不跨域清除（分类不清 item，item 不清 category）

---

## Task 3: 重构 ToolCategoryServiceImpl

**文件：** `xiaoyanQt_serve/src/main/java/com/yan/xiaoyanqtserve/service/impl/main/ToolCategoryServiceImpl.java`

**做什么：**

1. 注入 `ToolCacheManager`（替代直接注入 `RedisUtil`）
2. 删除所有缓存相关常量：
   - ~~`CACHE_KEY_PREFIX`~~
   - ~~`CACHE_KEY_ENABLED_LIST`~~
   - ~~`CACHE_EXPIRE_MINUTES`~~
3. 删除 `RedisUtil` 字段注入（`redisUtil`）
4. 重写 `getDetailById()` — 改用 `toolCacheManager.getCategoryDetail(id, () -> toolCategoryMapper.selectById(id))`
5. 重写 `getEnabledCategories()` — 改用 `toolCacheManager.getEnabledCategories(() -> { ... 查询 DB ... })`
6. 重写 `add()` / `update()` / `deleteById()` — 写操作后调用 `toolCacheManager.evictCategoryCache(id)` 替代原 `clearCache()`
7. 删除 `clearCache()` 方法

**不做什么：** 不改变接口签名，不改 `getList()`（分页查询本身不缓存）

---

## Task 4: 重构 ToolItemServiceImpl

**文件：** `xiaoyanQt_serve/src/main/java/com/yan/xiaoyanqtserve/service/impl/main/ToolItemServiceImpl.java`

**做什么：**

1. 注入 `ToolCacheManager`（替代直接注入 `RedisUtil`）
2. 删除所有缓存相关常量：
   - ~~`CACHE_KEY_PREFIX`~~
   - ~~`CACHE_KEY_ENABLED_LIST`~~
   - ~~`CACHE_KEY_GROUP_BY_CATEGORY`~~
   - ~~`CACHE_EXPIRE_MINUTES`~~
3. 删除 `RedisUtil` 字段注入（`redisUtil`）
4. 重写 `getDetailById()` — 改用 `toolCacheManager.getItemDetail(id, () -> toolItemMapper.selectById(id))`
5. 重写 `getEnabledItemsGroupByCategory()` — 改用 `toolCacheManager.getGroupedItems(() -> { ... 查询 DB 组装 ... })`
6. 重写 `add()` / `update()` / `deleteById()` — 写操作后调用 `toolCacheManager.evictItemCache(id)` 替代原 `clearCache()`
7. 删除 `clearCache()` 方法
8. `incrementClickCount()` 保持不变（不走缓存，直接操作 DB）

**不做什么：** 不改变接口签名，不改 `getList()` / `getItemsByCategoryId()`（当前未缓存）

---

## Task 5: 验证

**做什么：**

1. 检查编译是否通过（所有 import 正确、方法签名匹配）
2. 逻辑 Review：
   - 确认所有原来的缓存读取点都改为了 `ToolCacheManager` 调用
   - 确认所有原来的 `clearCache()` 调用都改为了精确 `evict*` 调用
   - 确认没有遗漏的 `RedisUtil` 直接调用（除了 `incrementClickCount` 中的 incr）
   - 确认 `ToolCategoryServiceImpl` 不再依赖 `RedisUtil`
   - 确认 `ToolItemServiceImpl` 不再依赖 `RedisUtil`
3. 确认 `_tmp_xiaoyanQt_serve_master_merge` 目录中的旧文件不需要同步修改（它是旧版本快照）

---

## 缓存行为对照表

| 操作 | 重构前 | 重构后 |
|------|--------|--------|
| 主站获取工具列表 | get(KEY_GROUP_BY_CATEGORY) → miss → 查DB → set | getGroupedItems(loader) → 同左，但空结果也缓存 5min |
| 主站获取工具详情 | get(KEY_ITEM_{id}) → miss → 查DB → set | getItemDetail(id, loader) → 同左，但空结果也缓存 5min |
| 管理后台新增工具 | save → clearCache()（全量清除 4 个 key + 2 个 pattern） | save → evictItemCache(null)（精确清除 2 个 key） |
| 管理后台修改工具 | updateById → clearCache()（全量清除） | updateById → evictItemCache(id)（精确清除 3 个 key） |
| 管理后台删除工具 | deleteById → clearCache()（全量清除） | deleteById → evictItemCache(id)（精确清除 3 个 key） |
| 管理后台新增分类 | save → clearCache()（清除 2 个 key） | save → evictCategoryCache(null)（精确清除 2 个 key） |
| 管理后台修改分类 | updateById → clearCache()（清除 2 个 key） | updateById → evictCategoryCache(id)（精确清除 3 个 key） |

**关键改进：** 工具变更时不再清除分类缓存，分类变更时不再清除工具项缓存。
