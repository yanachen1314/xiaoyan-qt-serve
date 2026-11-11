# xiaoyanQT系统

## Web管理端
- 后台管理端：[github（主）](https://github.com/yanachen1314/xiaoyan-qt-admin)、[gitee（副）](https://gitee.com/yanachen1314/xiaoyan-qt-admin)
- 用户Web端：[github（主）](https://github.com/yanachen1314/xiaoyan-qt-user)、[gitee（副）](https://gitee.com/yanachen1314/xiaoyan-qt-user)


## 后端服务

### 1、版本管理与部署
> 脚本文件、环境变量文件、docker-compose文件需要放在服务器某个可执行的目录下
#### （1）每次发布新版本，请在项目根目录下执行以下命令以设置版本号：
```shell
mvn versions:set "-DnewVersion=4.0.0" "-DgenerateBackupPoms=false"
```
#### （2）环境变量
环境变量的配置文件： [`.env`](./.env)
#### （3）Github Action 自动化构建镜像
- [docker-compose.yml](docker-compose.yml)
- [Dockerfile](Dockerfile)
#### （4）自定义脚本更新容器镜像
自定义脚本：[deploy-from-acr.sh](deploy-from-acr.sh)


### 2、项目技术栈
- Spring Boot3.5.9
- MyBatis Plus
- MySQL8
- Redis
- JWT
- Spring Security

### 3、功能模块
1、安全与认证模块
- API Key 认证: 通过自定义的 `ApiKeyAuthFilter` 实现。拦截 `/api/key/**` 请求，从 `X-API-Key` Header 中提取密钥并验证。验证成功后，将认证信息（如密钥值、`ROLE_KEY_USER`）放入 Spring Security 上下文。

- JWT 认证： 通过自定义的 `JwtAuthFilter` 实现。拦截 `/api/admin/**` 请求，从 `Authorization: Bearer <token>` Header 中提取 JWT 并验证。验证成功后，从 Token 或数据库加载管理员信息和角色（如 `ROLE_ADMIN`），并放入 Spring Security 上下文。

- Public公共接口: 提供 `/api/public/**` 请求，用于公开访问的功能，如管理员注册登录、密钥激活等。

2、密钥管理模块
- 密钥生成
- 密钥修改
- 密钥查询
- 密钥激活

3、AI处理模块
该模块通过接口`/api/key/deal/**`,处理前端的请求，调用大模型