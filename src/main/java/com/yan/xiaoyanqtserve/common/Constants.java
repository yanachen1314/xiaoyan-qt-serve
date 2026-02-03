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
            "http://localhost:8070",
            "http://127.0.0.1:8070");
    // 是否允许CORS跨域携带 Cookie
    public static final Boolean CORS_allowCredentials = true;

    // 定义Header名称常量
    public static final String HEADER_NAME = "X-API-Key";
    // 定义Header前缀
    public static String HEADER_PREFIX = "Bearer ";

    // 默认字符集
    public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
}
