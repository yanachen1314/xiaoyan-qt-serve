# 微信公众号回调迁移实现记录

## 目标

将 `xygoMain/server` 中当前微信公众号回调处理能力迁移到 `xiaoyanQt_serve`，保持相同的核心行为：

- `GET /api/wechat/callback`：明文模式校验 `signature`、`timestamp`、`nonce`，校验通过后返回 `echostr` 明文；安全模式校验 `msg_signature` 并解密 `echostr` 后返回明文。
- `POST /api/wechat/callback`：明文模式校验签名后解析微信 XML 消息体；安全模式先校验 `msg_signature`，再解密 `Encrypt` 节点后解析消息体。
- 关注事件：返回“感谢关注，验证码：xxxx”的被动回复文本 XML。
- 文本消息“豆包”：返回“您的回填验证码：xxxx”的被动回复文本 XML。
- 取消关注、扫码事件和暂未处理消息：返回 `success` 明文，不主动回复用户。
- `GET /api/wechat/health`：返回微信回调服务健康状态。
- 微信官方消息加解密参考：`https://developers.weixin.qq.com/doc/offiaccount/Message_Management/Message_encryption_and_decryption_instructions.html`

## 实现范围

- 新增微信配置项 `xiaoyan.wechat.token`、`xiaoyan.wechat.app-id`、`xiaoyan.wechat.encoding-aes-key`，分别支持通过 `WECHAT_TOKEN`、`WECHAT_APP_ID`、`WECHAT_ENCODING_AES_KEY` 环境变量覆盖。
- 新增微信回调 Controller、签名服务、XML 解析器、AES 加解密服务、消息实体和被动回复 XML 构建工具。
- 安全模式下使用 `msg_signature` 校验密文消息，使用 `EncodingAESKey` 进行 AES-CBC 解密，并校验明文末尾 `AppID`。
- 安全模式下如需要被动回复用户，会将回复 XML 重新加密为包含 `Encrypt`、`MsgSignature`、`TimeStamp`、`Nonce` 的 XML。
- 将 `/api/wechat/**` 加入请求类型枚举、系统模式过滤器白名单和 Spring Security 放行链。
- 对 XML 解析启用安全处理，禁用外部实体，避免 XXE 风险。
- 响应 XML 和文本时显式使用 UTF-8，保证中文验证码提示不乱码。

## 验证

- `mvn -Dtest=Wechat* test`
- `mvn test`

两组测试均已通过。
