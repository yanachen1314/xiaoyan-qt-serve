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

#### （5）本地构建并检查镜像
在项目根目录（包含 `pom.xml` 和 `Dockerfile` 的目录）执行：

```shell
mvn --batch-mode -DskipTests clean package
docker build -t xiaoyanqt-server:local .
docker run --rm --entrypoint sh xiaoyanqt-server:local -c 'test -f /app/app.jar && test -s /app/app.jar && test -r /app/app.jar && ls -l /app/app.jar'
```

Maven 生成的可执行 JAR 位于 `target/xiaoyanqt-serve-<版本号>.jar`，Dockerfile 将其复制为 `/app/app.jar`。构建前执行 `clean`，避免 `target/` 中残留多个版本的 JAR。

#### （6）排查 `Unable to access jarfile /app/app.jar`
该日志表示 Java 无法读取启动文件，应用尚未进入 Spring Boot 初始化阶段。先在服务器上检查报错容器实际使用的镜像和挂载（将 `xiaoyan_qt` 替换为实际容器名）：

```shell
docker inspect xiaoyan_qt --format 'image={{.Config.Image}} image_id={{.Image}} mounts={{json .Mounts}}'
```

以下命令在 Linux 服务器执行，用该容器的镜像 ID 启动临时容器，单独检查镜像中的 JAR：

```shell
docker run --rm --entrypoint sh "$(docker inspect --format '{{.Image}}' xiaoyan_qt)" -c 'test -f /app/app.jar && test -s /app/app.jar && test -r /app/app.jar && ls -l /app/app.jar'
```

- 如果镜像检查失败，核对镜像版本、JAR 文件权限与构建日志，确认镜像从 `target/` 复制了正确的可执行 JAR。
- 如果镜像检查成功，检查 1Panel 中的目录挂载和启动命令。不要将不含应用 JAR 的宿主机目录挂载到 `/app`，也不要用错误文件覆盖 `/app/app.jar`；这会遮蔽镜像中原有的 JAR。修改挂载后需要重新创建容器。

提交修复并确认 GitHub Actions 成功发布新镜像后，在服务器的部署目录执行以下命令；使用 1Panel 自定义编排时，需同时更新面板中实际使用的配置：

```shell
docker compose pull xiaoyanqt-server
docker compose up -d --force-recreate xiaoyanqt-server
docker compose logs --tail=100 xiaoyanqt-server
```


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
