package com.yan.xiaoyanqtserve.common;

import cn.hutool.core.collection.ListUtil;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 *
 * @author yanchen
 * @since 2026/2/3 17:14
 */
public class Constants {
    // 前端域名（测试环境、灰度、正式环境等） 白名单
    public static final List<String> ORIGINS_WHITE_LIST = ListUtil.of(
            "http://119.29.228.36:9999",
            "https://119.29.228.36:9999",
            "http://119.29.228.36",
            "https://119.29.228.36",
            "http://xygo.cn",
            "https://xygo.cn",
            "http://localhost:8070",
            "http://127.0.0.1:8070");
    // 是否允许CORS跨域携带 Cookie
    public static final Boolean CORS_allowCredentials = true;


    // 网站域名
    public static final String WEB_DOMAIN = "https://ai.xygo.cn";

    // 购买卡密外链地址
    public static final String SHOP_URL = "https://pay.ldxp.cn/shop/xiaoyanai";

    // 定义Header名称常量
    public static final String HEADER_KEY_NAME = "X-API-Key";
    public static final String HEADER_CLIENT_VERSION_NAME = "X-Client-Version";
    public static final String HEADER_DEVICE_TYPE = "X-Device";
    public static final String HEADER_DEVICE_ID = "X-Device-Id";
    public static final String HEADER_AUTH_NAME = "Authorization";
    // 定义Header前缀
    public static final String HEADER_PREFIX = "Bearer ";

    // 限流前缀Key
    public static final String RATE_LIMIT_PREFIX = "Rate_limit:";

    // Redis加锁前缀
    public static final String REDIS_LOCK_PREFIX = "Redis_lock:";

    // 默认字符集
    public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    // 系统配置
    public static final String CONFIG_KEY = "sysConfig:config";
    public static final String CONFIG_MODEL_LIST_KEY = "sysConfig:modelList";
    public static final String CONFIG_MODEL_LIST_PREFIX = "dashscope.model";
    // 百炼API KEY，config_name
    public static final String DASHSCOPE_API_KEY = "dashscope.apiKey";

    // 系统维护配置项目KEY
    public static final String SYSTEM_MAINTENANCE_KEY = "system.maintenance";
    // 系统维护配置项目的开启状态
    public static final String SYSTEM_MAINTENANCE_STATUS = "system.maintenance.status";

    // 客户端版本缓存KEY
    public static final String VERSION_KEY = "system.version";
    // LINK3地址缓存KEY
    public static final String LINK3_KEY = "system.updateLink";

    // HTTPRequest的请求属性  请求日志相关
    // 日志请求ID
    public static final String ATTRIBUTE_REQUEST_ID = "REQUEST_ID";
    // 请求开始时间
    public static final String ATTRIBUTE_START_TIME = "START_TIME";
    // 日志实体对象
    public static final String ATTRIBUTE_LOG_ENTITY = "LOG_ENTITY";
    // 业务码
    public static final String ATTRIBUTE_BIZ_CODE = "BIZ_CODE";
    // 响应消息
    public static final String ATTRIBUTE_RESPONSE_MESSAGE = "RESPONSE_MESSAGE";

    // 邮件账号发送统计前缀: mail:send:count:{date}:{emailAddress}
    public static final String MAIL_SEND_COUNT_PREFIX = "mail:send:count:";
    // 邮件账号发送成功统计前缀
    public static final String MAIL_SEND_SUCCESS_PREFIX = "mail:send:success:";
    // 邮件账号发送失败统计前缀
    public static final String MAIL_SEND_FAIL_PREFIX = "mail:send:fail:";
    // 邮件账号轮询索引Key
    public static final String MAIL_ACCOUNT_ROUND_ROBIN_KEY = "mail:account:round_robin";

    // 邮箱免费密钥池（存储免费密钥ID的Set集合，公共池）
    public static final String FREE_KEY_POOL = "free_key:pool";
    // 今日免费密钥发放数量统计
    public static final String FREE_KEY_DAILY_COUNT = "free_key:daily_count:";
    // 邮箱免费密钥的使用场景标识
    public static final String FREE_KEY_USAGE_TYPE = "apikey.usageType.emailFree";
    // 邮箱免费密钥的产品码
    public static final String FREE_KEY_PRODUCT_CODE = "EMAIL_FREE_KEY";
}
