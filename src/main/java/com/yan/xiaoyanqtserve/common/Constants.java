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
            "http://119.29.228.36",
            "https://119.29.228.36",
            "http://xygo.cn",
            "https://xygo.cn",
            "http://localhost:8070",
            "http://127.0.0.1:8070");
    // 是否允许CORS跨域携带 Cookie
    public static final Boolean CORS_allowCredentials = true;

    // 定义Header名称常量
    public static final String HEADER_KEY_NAME = "X-API-Key";
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
}
