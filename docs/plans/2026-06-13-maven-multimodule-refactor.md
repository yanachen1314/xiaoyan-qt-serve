# 后端 Maven 多模块重构计划书

## 背景

`xiaoyanQt_serve` 当前是单 Maven 模块 Spring Boot 后端，所有代码、配置、实体、Mapper、Service、Controller 和第三方依赖都集中在一个工程中。随着后台管理、卡密授权、邮件、工具服务、微信回调、系统配置等功能持续增加，单模块结构已经开始带来以下问题：

- 业务边界不够清晰，功能之间容易出现隐式依赖。
- 所有第三方依赖集中在根 `pom.xml`，无法按模块收敛依赖范围。
- 单元测试和集成测试缺少模块级归属，后续维护成本会逐渐升高。
- 后续如果需要进一步拆分服务或插件化能力，当前结构迁移成本较高。

本次重构目标不是拆成微服务，而是在保持“一个单体服务、一个启动入口、一个部署产物”的前提下，将后端整理为 Maven 多模块单体。

## 重构目标

1. 将后端拆分为职责清晰的 Maven 多模块结构。
2. 保留现有 Spring Boot 单体启动方式，最终仍由一个 `app` 模块打包运行。
3. 按业务能力垂直拆分模块，避免按 `controller/service/mapper` 横向拆分。
4. 通过 parent `pom.xml` 统一管理版本、插件和公共依赖声明。
5. 将模块依赖关系显式化，减少业务代码之间的无序引用。
6. 保持现有 API 路径、配置项、数据库表结构和部署方式基本不变。
7. 分阶段迁移，每阶段都能编译、测试和启动验证。

## 非目标

- 不拆微服务。
- 不改变数据库表结构。
- 不重写现有业务逻辑。
- 不变更现有前端调用路径。
- 不引入新的 RPC、消息队列或分布式事务。
- 第一阶段不单独拆出 `aigc` 模块，AIGC 能力先归入 `tool` 模块。

## 推荐模块结构

```text
xiaoyanQt_serve/
├─ pom.xml
├─ xiaoyanqt-common
├─ xiaoyanqt-infrastructure
├─ xiaoyanqt-system
├─ xiaoyanqt-license
├─ xiaoyanqt-mail
├─ xiaoyanqt-tool
├─ xiaoyanqt-wechat
└─ xiaoyanqt-app
```

### `pom.xml`

根工程只作为 Maven parent 和 aggregator：

- `packaging` 设置为 `pom`。
- 统一声明 `groupId`、`version`、`java.version`。
- 统一管理 Spring Boot、MyBatis Plus、JWT、Hutool、OpenAI、DashScope 等依赖版本。
- 统一管理 Maven 插件版本。
- 不放业务代码。

### `xiaoyanqt-common`

放置稳定、低依赖、可被所有模块复用的公共代码。

建议迁入：

- `common/dto`
- `common/vo`
- `common/enums`
- `common/annotaions`
- `common/exception/BizException.java`
- `common/utils/ApiResponse.java`
- `common/Constants.java`
- 纯工具类，例如 `IdUtil`、`NetUtil`、`MainUtil`、`VersionCheckUtil`、`KeysUtil`、`SceneIdEncryptor`

不建议迁入：

- `GlobalExceptionHandler`
- `RedisUtil`
- `RedisLockUtil`
- `SecurityContextUtil`
- `SecurityResponseUtil`
- AOP、Filter、Interceptor、Spring 配置类

这些类带有明显运行时基础设施属性，应放入 `infrastructure` 或 `app`。

### `xiaoyanqt-infrastructure`

放置基础设施、框架集成和横切能力。

建议迁入：

- `config/frame/MybatisPlusConfig.java`
- `config/frame/RedisConfig.java`
- `config/frame/WebConfig.java`
- `config/ThreadPoolConfig.java`
- `aop/RateLimiterAspect.java`
- `aop/CaptchaValidationAspect.java`
- `filter/JwtAuthFilter.java`
- `filter/ApiKeyAuthFilter.java`
- `filter/SystemModeFilter.java`
- `interceptor/RequestLoggingInterceptor.java`
- `common/exception/GlobalExceptionHandler.java`
- `common/utils/RedisUtil.java`
- `common/utils/RedisLockUtil.java`
- `common/utils/JwtUtil.java`
- `common/utils/SecurityContextUtil.java`
- `common/utils/SecurityResponseUtil.java`

注意：如果某个基础设施类依赖具体业务服务，应优先通过接口反转或暂时留在 `app`，避免让 `infrastructure` 反向依赖业务模块。

### `xiaoyanqt-system`

系统与后台基础能力模块。

建议迁入：

- `entity/AdminUser.java`
- `entity/SysConfig.java`
- `entity/ProductType.java`
- `entity/RequestLog.java`
- `mapper/AdminUserMapper.java`
- `mapper/SysConfigMapper.java`
- `mapper/ProductTypeMapper.java`
- `mapper/RequestLogMapper.java`
- `service/AdminUserService.java`
- `service/SysConfigService.java`
- `service/ProductTypeService.java`
- `service/RequestLogService.java`
- `service/impl/sys/AdminUserServiceImpl.java`
- `service/impl/sys/SysConfigServiceImpl.java`
- `service/impl/sys/ProductTypeServiceImpl.java`
- `service/impl/sys/RequestLogServiceImpl.java`
- `service/impl/system/ClientVersionService.java`
- `service/impl/system/MaintenanceService.java`
- `security/AdminUserDetails.java`
- `security/AdminUserDetailsService.java`
- 后台用户、系统配置、产品类型、请求日志相关 Controller

该模块负责后台账号、系统配置、维护模式、版本检查、产品类型、请求日志等基础业务。

### `xiaoyanqt-license`

卡密和 API Key 授权模块。

建议迁入：

- `entity/KeyInfo.java`
- `mapper/KeyInfoMapper.java`
- `service/KeyInfoService.java`
- `service/impl/sys/KeyInfoServiceImpl.java`
- `service/impl/apikey/KeyInfoManagerService.java`
- `service/impl/apikey/ApiKeyInfoService.java`
- `common/utils/LicenseKeyGenerator.java`
- `common/utils/KeysUtil.java`，如果不放 common
- `controller/admin/KeyInfoController.java`
- `controller/api/ApiKeyController.java`
- `controller/publicApi/KeyPublicController.java`
- `src/main/resources/key/private.pem`
- `src/main/resources/key/public.pem`

该模块负责卡密生成、激活、校验、免费卡密池、API Key 查询与过滤支撑。

### `xiaoyanqt-mail`

邮件账号、邮件申请、邮件发送和邮件模板模块。

建议迁入：

- `entity/MailAccount.java`
- `entity/MailApplyRecord.java`
- `entity/MailSendRecord.java`
- `mapper/MailAccountMapper.java`
- `mapper/MailApplyRecordMapper.java`
- `mapper/MailSendRecordMapper.java`
- `service/MailAccountService.java`
- `service/MailApplyRecordService.java`
- `service/MailSendRecordService.java`
- `service/impl/sys/MailAccountServiceImpl.java`
- `service/impl/sys/MailApplyRecordServiceImpl.java`
- `service/impl/sys/MailSendRecordServiceImpl.java`
- `service/impl/mail/MailAccountManagerService.java`
- `service/impl/mail/MailApplyManagerService.java`
- `service/impl/mail/MailPushService.java`
- `service/impl/mail/TemplateService.java`
- `config/MultiMailSenderConfig.java`
- `config/custome/MailPropertiesConfig.java`
- `task/MailAccountTask.java`
- `task/MailRecordSyncTask.java`
- `templates/mail/**`
- 邮件账号、邮件申请、邮件发送记录相关 Controller

该模块可以依赖 `xiaoyanqt-license`，因为免费卡密申请流程会用到卡密池和卡密归还逻辑。

### `xiaoyanqt-tool`

主站工具、工具授权、豆包代理和第一阶段 AIGC 能力模块。

建议迁入：

- `entity/ToolCategory.java`
- `entity/ToolItem.java`
- `mapper/ToolCategoryMapper.java`
- `mapper/ToolItemMapper.java`
- `service/ToolCategoryService.java`
- `service/ToolItemService.java`
- `service/impl/main/ToolCategoryServiceImpl.java`
- `service/impl/main/ToolItemServiceImpl.java`
- `service/impl/main/ToolCacheManager.java`
- `service/impl/tool/ToolAuthService.java`
- `service/impl/tool/ToolJwtService.java`
- `service/impl/tool/ToolTokenClaims.java`
- `service/impl/tool/DoubaoToolProxyService.java`
- `service/impl/model/ModelService.java`
- `service/impl/model/AigcProcessingService.java`
- `config/custome/ToolAuthPropertiesConfig.java`
- `config/custome/DoubaoToolPropertiesConfig.java`
- `controller/main/MainToolController.java`
- `controller/main/MainToolAuthController.java`
- `controller/main/MainDoubaoController.java`
- 后台工具分类、工具项相关 Controller

说明：第一阶段不新建 `xiaoyanqt-aigc`。`ModelService`、`AigcProcessingService` 和 AIGC DTO 先跟随 `tool` 模块，等模型能力变复杂后再独立拆出。

### `xiaoyanqt-wechat`

微信公众号回调模块。

建议迁入：

- `controller/wechat/WechatController.java`
- `service/impl/wechat/WechatCallbackService.java`
- `service/impl/wechat/WechatCryptoService.java`
- `service/impl/wechat/WechatMessageParser.java`
- `service/impl/wechat/WechatReplyBuilder.java`
- `service/impl/wechat/WechatSignatureService.java`
- `common/dto/wechat/WechatMessage.java`
- `config/custome/WechatPropertiesConfig.java`

该模块当前会调用工具授权逻辑，因此允许依赖 `xiaoyanqt-tool`。

### `xiaoyanqt-app`

启动与装配模块。

建议保留或迁入：

- `XiaoyanQtServeApplication.java`
- `application.yaml`
- `application-dev.yaml`
- `application-prod.yaml`
- `mybatis-config.xml`
- `config/frame/SecurityConfig.java`，也可以后续下沉到 `infrastructure`
- `config/custome/CorsConfigProperties.java`
- `config/custome/WebInfoPropertiesConfig.java`
- `config/custome/XiaoyanSecurityConfig.java`
- `controller/TestController.java`，后续可删除或迁入专门测试模块
- `docker-compose.yml`
- Spring Boot 打包插件

`xiaoyanqt-app` 是最终运行模块，依赖所有业务模块和基础设施模块。

## 模块依赖关系

推荐依赖关系如下：

```text
xiaoyanqt-app
 ├─ xiaoyanqt-infrastructure
 ├─ xiaoyanqt-system
 ├─ xiaoyanqt-license
 ├─ xiaoyanqt-mail
 ├─ xiaoyanqt-tool
 └─ xiaoyanqt-wechat

xiaoyanqt-infrastructure
 └─ xiaoyanqt-common

xiaoyanqt-system
 ├─ xiaoyanqt-common
 └─ xiaoyanqt-infrastructure

xiaoyanqt-license
 ├─ xiaoyanqt-common
 ├─ xiaoyanqt-infrastructure
 └─ xiaoyanqt-system

xiaoyanqt-mail
 ├─ xiaoyanqt-common
 ├─ xiaoyanqt-infrastructure
 ├─ xiaoyanqt-system
 └─ xiaoyanqt-license

xiaoyanqt-tool
 ├─ xiaoyanqt-common
 ├─ xiaoyanqt-infrastructure
 ├─ xiaoyanqt-system
 └─ xiaoyanqt-license

xiaoyanqt-wechat
 ├─ xiaoyanqt-common
 ├─ xiaoyanqt-infrastructure
 └─ xiaoyanqt-tool
```

如果迁移过程中发现循环依赖，优先采用以下处理方式：

1. 将稳定 DTO、枚举、响应对象下沉到 `common`。
2. 将跨模块调用收敛为 Service 接口。
3. 将只用于装配的依赖上移到 `app`。
4. 暂时保留少量类在原模块，避免为了拆模块强行重写业务。

## 包名策略

第一阶段建议保持现有根包名：

```text
com.yan.xiaoyanqtserve
```

原因：

- Spring Boot 默认扫描范围不需要大改。
- 迁移时 import 改动更少。
- 降低一次性重构风险。

模块内部可以逐步演进为更清晰的子包，例如：

```text
com.yan.xiaoyanqtserve.tool
com.yan.xiaoyanqtserve.mail
com.yan.xiaoyanqtserve.license
com.yan.xiaoyanqtserve.wechat
```

但不建议在第一阶段同时做“大规模改包名”和“多模块迁移”，否则冲突面过大。

## Maven 配置方案

### 根 `pom.xml`

根 `pom.xml` 改为：

- `packaging=pom`
- `modules` 声明所有子模块
- `dependencyManagement` 管理版本
- `pluginManagement` 管理插件

示例：

```xml
<packaging>pom</packaging>

<modules>
    <module>xiaoyanqt-common</module>
    <module>xiaoyanqt-infrastructure</module>
    <module>xiaoyanqt-system</module>
    <module>xiaoyanqt-license</module>
    <module>xiaoyanqt-mail</module>
    <module>xiaoyanqt-tool</module>
    <module>xiaoyanqt-wechat</module>
    <module>xiaoyanqt-app</module>
</modules>
```

### 子模块 `pom.xml`

普通模块使用 `jar` 打包，不配置 `spring-boot-maven-plugin` 的 repackage。

只有 `xiaoyanqt-app` 使用 Spring Boot 打包插件，生成最终可运行 jar。

### 依赖收敛原则

- `common` 不依赖 Spring Web、Redis、MyBatis、Security。
- `infrastructure` 可以依赖 Spring、Redis、MyBatis、Security。
- `mail` 才声明 `spring-boot-starter-mail` 和 `spring-boot-starter-thymeleaf`。
- `tool` 才声明 OpenAI、DashScope、Spring Retry 等 AI/模型相关依赖。
- `license` 才声明 JWT/RSA/加密相关强依赖，公共 JWT 工具除外。
- `app` 依赖各业务模块，不直接承载业务实现。

## MyBatis 与资源迁移

Mapper XML 建议随所属业务模块移动：

```text
xiaoyanqt-system/src/main/resources/mapper/system/*.xml
xiaoyanqt-license/src/main/resources/mapper/license/*.xml
xiaoyanqt-mail/src/main/resources/mapper/mail/*.xml
xiaoyanqt-tool/src/main/resources/mapper/tool/*.xml
```

`application.yaml` 中配置：

```yaml
mybatis-plus:
  mapper-locations: classpath*:mapper/**/*.xml
```

`@MapperScan` 建议改为：

```java
@MapperScan("com.yan.xiaoyanqtserve.**.mapper")
```

如果通配扫描存在兼容问题，则使用多个明确包路径。

## 分阶段实施计划

### 阶段 0：准备与基线确认

目标：确认当前单模块状态可编译、可测试、可运行。

任务：

1. 执行当前项目测试。
2. 执行当前项目打包。
3. 记录当前启动方式、环境变量、配置文件和运行命令。
4. 确认当前 Git 工作区状态，避免覆盖已有改动。

验收标准：

- 当前单模块可以正常 `mvn test`。
- 当前单模块可以正常 `mvn package`。
- 记录基线问题，后续迁移不把历史问题误判为重构引入。

### 阶段 1：建立 Maven 多模块骨架

目标：先建立父子模块结构，但尽量少移动业务代码。

任务：

1. 将根 `pom.xml` 改为 parent aggregator。
2. 新建各子模块目录和基础 `pom.xml`。
3. 新建 `xiaoyanqt-app`，先承接原 Spring Boot 打包职责。
4. 暂时保留原代码位置或整体迁入 `app`，确保结构调整后仍可编译。

验收标准：

- 根目录执行 `mvn test` 成功。
- 根目录执行 `mvn package` 成功。
- 最终可运行 jar 由 `xiaoyanqt-app` 生成。

### 阶段 2：迁移 `common`

目标：先抽离稳定公共代码，降低后续业务模块迁移成本。

任务：

1. 迁移 DTO、VO、Enum、注解、`BizException`、`ApiResponse`、`Constants`。
2. 迁移无 Spring 运行时依赖的纯工具类。
3. 各模块通过依赖 `xiaoyanqt-common` 引用公共代码。
4. 修正 import 与测试。

验收标准：

- `common` 不依赖 Web、Redis、MyBatis、Security。
- 根目录 `mvn test` 通过。
- 没有业务实现类被误放入 `common`。

### 阶段 3：迁移 `infrastructure`

目标：抽离基础设施能力和横切逻辑。

任务：

1. 迁移 Redis、MyBatis、Web 基础配置。
2. 迁移 AOP、Filter、Interceptor。
3. 迁移 `RedisUtil`、`RedisLockUtil`、`JwtUtil`、安全响应工具。
4. 处理 `ApiKeyAuthFilter` 对 `license` 模块的依赖关系。
5. 如出现反向依赖，优先将 Filter 留在 `app`，待业务模块稳定后再下沉。

验收标准：

- 基础设施模块依赖关系清晰。
- Spring Bean 可以被启动模块扫描到。
- 根目录 `mvn test` 和 `mvn package` 通过。

### 阶段 4：迁移 `system`

目标：迁移后台基础能力，为其他业务模块提供系统配置、用户和日志能力。

任务：

1. 迁移后台用户、系统配置、产品类型、请求日志实体、Mapper、Service、Controller。
2. 迁移维护模式和版本检查服务。
3. 迁移后台用户认证相关实现。
4. 修正 `SecurityConfig` 对认证服务的依赖。

验收标准：

- 后台登录、系统配置、产品类型、请求日志相关测试通过。
- `SysConfigServiceImplTest` 通过。
- 系统配置能力可供其他模块正常注入。

### 阶段 5：迁移 `license`

目标：迁移卡密和 API Key 授权能力。

任务：

1. 迁移 `KeyInfo` 实体、Mapper、Service。
2. 迁移 `KeyInfoManagerService` 和 `ApiKeyInfoService`。
3. 迁移卡密公有接口、后台卡密接口、API Key 接口。
4. 迁移 RSA key 资源。
5. 调整 `ApiKeyAuthFilter` 依赖。

验收标准：

- 卡密生成、激活、校验逻辑保持不变。
- API Key 过滤逻辑保持不变。
- 免费卡密池相关 Redis key 行为保持不变。
- 根目录 `mvn test` 通过。

### 阶段 6：迁移 `mail`

目标：迁移邮件账号、邮件申请、发送记录和模板能力。

任务：

1. 迁移邮件相关实体、Mapper、Service、Controller。
2. 迁移 `MailPushService`、`TemplateService`、`MailApplyManagerService`。
3. 迁移邮件模板资源。
4. 迁移邮件定时任务。
5. 处理 `mail` 对 `license` 的依赖。

验收标准：

- 邮件模板可以被 Thymeleaf 正确加载。
- 邮件发送记录、申请记录和账号管理接口保持不变。
- 定时任务可以被 Spring 正确发现。
- 根目录 `mvn test` 通过。

### 阶段 7：迁移 `tool`

目标：迁移主站工具、工具授权、豆包代理和第一阶段 AIGC 能力。

任务：

1. 迁移工具分类、工具项实体、Mapper、Service、Controller。
2. 迁移 `ToolCacheManager`。
3. 迁移 `ToolAuthService`、`ToolJwtService`、`DoubaoToolProxyService`。
4. 迁移 `ModelService`、`AigcProcessingService`。
5. 迁移工具授权和豆包相关配置属性。
6. 处理 `tool` 对 `license`、`system` 的依赖。

验收标准：

- `ToolAuthServiceTest` 和 `ToolJwtServiceTest` 通过。
- 主站工具列表、工具详情、工具授权接口保持不变。
- 豆包代理和 AIGC 处理逻辑保持不变。
- OpenAI、DashScope 依赖只在 `tool` 模块声明。

### 阶段 8：迁移 `wechat`

目标：迁移微信公众号回调能力。

任务：

1. 迁移微信 Controller 和相关 Service。
2. 迁移微信配置属性。
3. 处理 `wechat` 对 `tool` 的依赖。
4. 调整微信测试包路径和依赖。

验收标准：

- `WechatCallbackServiceTest` 通过。
- `WechatControllerTest` 通过。
- `WechatCryptoServiceTest`、`WechatMessageParserTest`、`WechatSignatureServiceTest` 通过。
- 微信回调验签、解密、消息解析和回复行为保持不变。

### 阶段 9：清理与收敛

目标：清理过渡代码，收敛依赖和文档。

任务：

1. 删除原单模块遗留空目录。
2. 清理未使用 import 和重复依赖。
3. 检查各模块是否存在不合理依赖。
4. 更新 README、启动说明和开发规范。
5. 更新 CI 或本地构建命令。

验收标准：

- 根目录 `mvn test` 通过。
- 根目录 `mvn package` 通过。
- `xiaoyanqt-app` 可以正常启动。
- 各模块职责和依赖关系与本计划基本一致。

## 测试策略

### 编译与单元测试

每完成一个阶段执行：

```bash
mvn test
```

最终执行：

```bash
mvn clean package
```

### 重点回归范围

- 后台登录与 JWT 鉴权。
- API Key Filter。
- 系统维护模式 Filter。
- 工具列表、工具详情、工具缓存。
- 工具验证码换取 Tool Token。
- 豆包工具代理。
- 卡密激活、校验、免费卡密池。
- 邮件发送、模板渲染、邮件申请流程。
- 微信回调验签、解密、自动回复。
- 系统配置读取与缓存。

### 自动化测试优先级

优先保证已有测试迁移成功：

- `SysConfigServiceImplTest`
- `ToolAuthServiceTest`
- `ToolJwtServiceTest`
- `WechatCallbackServiceTest`
- `WechatControllerTest`
- `WechatCryptoServiceTest`
- `WechatMessageParserTest`
- `WechatSignatureServiceTest`

如时间允许，补充：

- `KeyInfoManagerService` 卡密校验测试。
- `MailApplyManagerService` 免费卡密申请补偿测试。
- `ToolCacheManager` 缓存命中、未命中和写失效测试。

## 风险与应对

| 风险 | 影响 | 应对 |
|---|---|---|
| 模块间循环依赖 | Maven 无法编译 | DTO/接口下沉到 `common`，装配上移到 `app` |
| Spring Bean 扫描不到 | 启动失败 | 保持根包名，启动类扫描 `com.yan.xiaoyanqtserve` |
| Mapper XML 加载不到 | 数据库访问失败 | 使用 `classpath*:mapper/**/*.xml` |
| 资源文件迁移遗漏 | 邮件模板、RSA key 加载失败 | 按模块维护资源清单，迁移后执行启动验证 |
| 依赖声明缺失 | 子模块编译失败 | 按模块逐步迁移，每阶段跑 `mvn test` |
| `common` 膨胀 | 模块边界失效 | 禁止业务 Service、Spring 配置、基础设施工具进入 `common` |
| 一次性改包名过多 | 冲突面扩大 | 第一阶段保留原根包名，后续再渐进改包 |

## 建议提交策略

建议按阶段拆分提交，避免一个巨大提交难以 review：

1. `chore: add maven multi-module skeleton`
2. `refactor: extract common module`
3. `refactor: extract infrastructure module`
4. `refactor: extract system module`
5. `refactor: extract license module`
6. `refactor: extract mail module`
7. `refactor: extract tool module`
8. `refactor: extract wechat module`
9. `docs: update backend module structure`

## 最终验收清单

- [ ] 根工程是 Maven parent，`packaging=pom`。
- [ ] `xiaoyanqt-app` 是唯一 Spring Boot 可运行模块。
- [ ] 最终部署产物仍为一个后端服务 jar。
- [ ] 所有现有 API 路径保持不变。
- [ ] 所有配置文件仍能按 profile 正常加载。
- [ ] MyBatis Mapper XML 全部能被加载。
- [ ] 邮件模板和 RSA key 资源能被加载。
- [ ] `mvn test` 通过。
- [ ] `mvn clean package` 通过。
- [ ] 本地启动成功。
- [ ] 后台管理、卡密、邮件、工具、微信回调核心流程回归通过。

## 后续演进

第一阶段完成后，可以根据代码增长情况继续演进：

- 将 `tool` 中的 `ModelService`、`AigcProcessingService` 独立为 `xiaoyanqt-aigc`。
- 将 admin Controller 聚合到单独的 `xiaoyanqt-admin-api`，如果后台管理接口继续增多。
- 将公共 API Controller 聚合到 `xiaoyanqt-public-api`，如果开放接口继续增多。
- 将基础设施自动配置改造成更标准的 Spring Boot starter 风格。
- 建立模块依赖检查规则，防止业务模块之间随意互相引用。
