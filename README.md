# xiaoyanQT系统

## Web管理端
[Web管理端](https://github.com/xiaoyan-qt/xiaoyanqt-web)后台管理


## 后端服务

### 1、项目技术栈
- Spring Boot3.5.9
- MyBatis Plus
- MySQL8
- Redis
- JWT
- Spring Security

### 2、功能模块
1.密钥管理模块
- 密钥生成:
- 密钥修改
- 密钥查询
- 密钥激活

2.安全与认证模块
- API Key 认证: 通过自定义的 `ApiKeyAuthFilter` 实现。拦截 `/api/key/**` 请求，从 `X-API-Key` Header 中提取密钥并验证。验证成功后，将认证信息（如密钥值、`ROLE_KEY_USER`）放入 Spring Security 上下文。

- JWT 认证： 通过自定义的 `JwtAuthFilter` 实现。拦截 `/api/admin/**` 请求，从 `Authorization: Bearer <token>` Header 中提取 JWT 并验证。验证成功后，从 Token 或数据库加载管理员信息和角色（如 `ROLE_ADMIN`），并放入 Spring Security 上下文。

- Public公共接口: 提供 `/api/public/**` 请求，用于公开访问的功能，如注册、登录等。
