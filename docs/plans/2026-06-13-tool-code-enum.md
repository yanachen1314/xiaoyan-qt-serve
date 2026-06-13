# 工具编码枚举校验 Implementation Plan

> **For implementer:** Use TDD throughout. Write failing test first. Watch it fail. Then implement.

**Goal:** 认证接口继续接收前端传入的 `toolCode`，但只允许匹配后端枚举中的官方工具编码。

**Architecture:** 移除上一版 `tool_item.tool_code` 数据库字段相关代码；新增工具编码枚举作为白名单；`MainToolAuthController` 在调用 `ToolAuthService` 前先通过枚举校验并使用规范化后的编码。

**Tech Stack:** Spring Boot、JUnit 5、Mockito、MockMvc。

---

### Task 1: 枚举白名单

**Files:**
- Create: `src/main/java/com/yan/xiaoyanqtserve/common/enums/ToolCodeEnum.java`
- Create: `src/test/java/com/yan/xiaoyanqtserve/tool/ToolCodeEnumTest.java`

**Steps:**
1. 写失败测试：`doubao` 返回规范编码，空编码和未知编码抛业务异常。
2. 实现枚举和校验方法。
3. 跑目标测试。

### Task 2: 认证接口改用枚举校验

**Files:**
- Modify: `src/main/java/com/yan/xiaoyanqtserve/controller/main/MainToolAuthController.java`
- Modify: `src/test/java/com/yan/xiaoyanqtserve/tool/MainToolAuthControllerTest.java`

**Steps:**
1. 写失败测试：Controller 构造不再需要 `ToolItemService`；合法编码调用 `ToolAuthService`；非法编码不会调用。
2. 实现 Controller 枚举校验。
3. 跑目标测试。

### Task 3: 移除数据库字段相关代码

**Files:**
- Modify: `src/main/java/com/yan/xiaoyanqtserve/entity/ToolItem.java`
- Modify: `src/main/java/com/yan/xiaoyanqtserve/common/dto/main/ToolItemDto.java`
- Modify: `src/main/java/com/yan/xiaoyanqtserve/common/dto/main/ToolListResponse.java`
- Modify: `src/main/java/com/yan/xiaoyanqtserve/controller/main/MainToolController.java`
- Modify: `src/main/java/com/yan/xiaoyanqtserve/service/ToolItemService.java`
- Modify: `src/main/java/com/yan/xiaoyanqtserve/service/impl/main/ToolItemServiceImpl.java`

**Steps:**
1. 删除 `toolCode` 属性、列表响应输出、后台工具管理校验、ToolItemService 数据库查询方法。
2. 删除上一版数据库方案测试。
3. 跑全量测试。

### Task 4: SQL

**Files:**
- Final response only.

**Steps:**
1. 输出删除 `tool_code` 相关索引、约束、字段的 SQL。
