# 后端多模块运行说明

后端采用 Maven 多模块单体结构：

- `xiaoyanqt-common`：低依赖公共代码。
- `xiaoyanqt-infrastructure`：Redis、MyBatis、线程池、JWT 和密钥加载。
- `xiaoyanqt-system`、`xiaoyanqt-license`、`xiaoyanqt-mail`、`xiaoyanqt-tool`、`xiaoyanqt-wechat`：领域模块。
- `xiaoyanqt-app`：唯一的 Spring Boot 启动模块和 HTTP 入口层。

## 构建

在后端项目根目录执行完整校验：

```powershell
mvn clean verify
```

构建部署产物：

```powershell
mvn -pl xiaoyanqt-app -am package
```

仅 `xiaoyanqt-app` 会执行 Spring Boot repackage。部署时使用唯一产物：

```text
xiaoyanqt-app/target/xiaoyanqt-app-3.2.0.jar
```

其余模块会作为嵌套依赖 Jar 包含在该可执行 Jar 中，无需分别部署。

## 运行

使用测试配置启动：

```powershell
java -jar xiaoyanqt-app/target/xiaoyanqt-app-3.2.0.jar --spring.profiles.active=test
```

实际环境须通过环境变量提供数据库、Redis、邮件、验证码和第三方服务凭据；不得将真实凭据提交到仓库。
