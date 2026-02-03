### **核心功能模块**

1.  **密钥管理系统 (Key Management System)**
    *   **密钥生成**:
        *   管理员可以在后台（`/admin/**`）生成新的 32 位密钥。
        *   支持设置密钥的总使用次数（`total_uses`），0 表示无限制。
        *   支持设置密钥的产品类型/套餐（`product_type`）。
        *   支持设置备注信息（`notes`）。
        *   生成的密钥自动存储到 `key_info` 表中，初始状态为“启用”（`status = 1`），`remaining_uses` 等于 `total_uses`。
    *   **密钥修改**:
        *   管理员可以修改现有密钥的状态（启用/禁用）。
        *   管理员可以修改现有密钥的总使用次数（`total_uses`），修改后 `remaining_uses` 会相应更新。
    *   **密钥查询**:
        *   管理员可以分页查询所有密钥信息，支持按状态、产品类型等条件筛选。
    *   **密钥激活**:
        *   提供公开的 `/api/active` 接口，客户端可以通过 POST 请求将 32 位密钥发送过来进行激活。
        *   激活操作会将密钥的 `status` 从“启用”变为“已激活”（或保持“启用”但记录 `activated_time`，具体取决于业务逻辑，但当前实现是设置 `activated_time`）。

2.  **对外服务接口 (Public API)**
    *   **密钥激活接口**:
        *   **路径**: `/api/active`
        *   **方法**: `POST`
        *   **功能**: 接收客户端提交的 32 位密钥，进行激活操作（更新数据库记录）。
        *   **认证**: **无需**管理员登录，属于公开接口。
    *   **核心功能接口**:
        *   **路径**: `/api/ai/deal` (示例路径，代表系统唯一核心功能)
        *   **方法**: `POST`
        *   **功能**: 这是系统提供的唯一核心服务（例如，AIGC 降重）。客户端请求此接口时，系统会验证其携带的 API Key。
        *   **认证与授权**:
            *   客户端必须在请求头（`X-API-Key`）中携带有效的 32 位密钥。
            *   通过 `ApiKeyAuthFilter` 验证密钥的有效性（状态、次数、过期时间）。
            *   验证成功后，**原子性地扣减**该密钥的 `remaining_uses`。
            *   执行核心业务逻辑（如 AIGC 处理）并返回结果。
        *   **认证**: **无需**管理员登录，但**必须**提供有效的 API Key。

3.  **管理员后台 (Admin Backend)**
    *   **管理员身份认证**:
        *   提供 `/admin/login` 接口用于管理员登录。
        *   登录成功后，系统返回一个 JWT (JSON Web Token)。
        *   使用 `JwtAuthFilter` 解析和验证 JWT，加载管理员信息和角色。
    *   **管理员注册**:
        *   提供 `/admin/register` 接口用于管理员注册。
        *   注册成功后，系统返回一个 成功的响应。
    *   **管理员权限控制**:
        *   所有 `/admin/**` 路径下的接口都需要管理员登录，并且拥有 `ROLE_ADMIN` 角色才能访问。
        *   通过 Spring Security 的 `hasRole` 配置实现。
    *   **管理界面功能**:
        *   管理员可以登录后访问 `/admin/**` 下的管理界面（具体界面由前端实现，后端提供 API）。
        *   可以执行密钥生成、修改、查询等操作（对应上述“密钥管理系统”的功能）。

4.  **安全与认证模块 (Security & Authentication Module)**
    *   **API Key 认证**:
        *   通过自定义的 `ApiKeyAuthFilter` 实现。
        *   拦截 `/api/**` 请求，从 `X-API-Key` Header 中提取密钥并验证。
        *   验证成功后，将认证信息（如密钥值、`ROLE_KEY_USER`）放入 Spring Security 上下文。
    *   **JWT 认证**:
        *   通过自定义的 `JwtAuthFilter` 实现。
        *   拦截 `/admin/**` 请求，从 `Authorization: Bearer <token>` Header 中提取 JWT 并验证。
        *   验证成功后，从 Token 或数据库加载管理员信息和角色（如 `ROLE_ADMIN`），并放入 Spring Security 上下文。
    *   **Spring Security 配置**:
        *   配置了无状态会话 (`STATELESS`)。
        *   禁用了不必要的默认安全配置（如 CSRF、Form Login、HTTP Basic）。
        *   定义了 URL 路径与访问权限的映射关系（`/admin/login`, `/api/**`, `/admin/**`）。
        *   注册了 `ApiKeyAuthFilter` 和 `JwtAuthFilter` 到过滤器链。

5.  **数据存储与访问 (Data Storage & Access)**
    *   **数据库设计**:
        *   `admin_user`: 存储管理员信息。
        *   `key_info`: 存储所有密钥的详细信息（值、状态、次数、类型、激活时间、过期时间等）。
        *   `request_log`: 记录 API 请求日志。
    *   **ORM 框架**: 使用 MyBatis Plus 进行数据库操作，提供便捷的 CRUD 和高级查询功能。

6.  **核心业务逻辑 (Core Business Logic)**
    *   **请求处理服务**: `RequestProcessingService` 负责处理 `/api/ai/deal` 请求，包括 API Key 验证、次数扣减和核心业务执行（如 AIGC 降重）。
    *   **密钥管理服务**: `KeyManagementService` 负责密钥的生成、修改、查询和激活逻辑。

这个项目的核心围绕着**密钥的生命周期管理**和**基于密钥的访问控制**展开，同时为管理员提供了独立的、安全的后台管理入口。


### 代码文件说明

（1）BizException.java：自定义业务异常

（2）ApiResponse.java：业务统一响应格式（一般情况下，接口直接返回由该类包装的数据即可）

（3）GlobalExceptionHandler.java：全局异常处理器

辅助文件：Spring响应工具类（需要自定义Springboot响应时调用，ResponseUtil.java），业务状态码枚举类（BizStatus.java）

### 后端开发规范

编程风格方面：

- 可以使用JDK17的新特性

- 严格按照面向对象的设计原则，合理运用设计模式
- 保持代码的简洁性和可读性，避免过度复杂的嵌套结构
- 重要的业务逻辑要有详细的中文注释说明

数据处理方面：

- 所有的数据库操作都要使用MyBatis-Plus的条件构造器，防止SQL注入
- 对于用户输入的数据必须进行严格的校验，包括长度限制、格式验证、范围检查等
- 分页查询要使用MyBatis-Plus的分页插件
- 文件上传功能要限制文件大小和类型，文件存储在本地，路径为后端服务的根目录下的upload文件夹，即LoveWatch-serve/upload/

异常处理方面：

- 建立统一的异常处理机制，不要让异常信息直接暴露给前端
- 为不同类型的业务异常创建专门的异常类
- 在事务操作中要正确设置回滚规则
- 记录必要的操作日志和错误日志

API设计方面：

- RESTful API的URL设计要语义化，比如：
    - 获取用户列表用GET /api/users，如果是获取某个用户的信息用GET /api/users/get?uid=1
    - 分页请求用POST /api/users/page，请求体为分页请求参数，示例：{currentPage=1, pageSize=10, name="张三"}
    - 创建用户用POST /api/users/add，请求体为用户信息
    - 创建用户用POST /api/users/update，请求体为用户更新的信息
    - 删除用户用POST /api/users/delete，请求体为删除参数，示例：{uid=1}
- 返回数据格式要统一，包含状态码、消息和实际数据
- 对于敏感操作如删除、修改要添加权限验证
### 代码质量要求

- 代码要有良好的层次结构，各层职责分明
- 相似的功能要抽取公共方法或组件
- 核心功能要有使用说明，方法、接口等要有注释
- API接口要有详细的参数说明
- 数据库表结构要有字段说明文档

请按照这些要求生成完整可用的代码，确保各个模块能够协调工作，形成一个功能完善的系统。