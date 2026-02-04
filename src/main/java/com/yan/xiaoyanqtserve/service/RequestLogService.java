package com.yan.xiaoyanqtserve.service;

import com.yan.xiaoyanqtserve.entity.RequestLog;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * API请求日志表 服务类
 * </p>
 *
 * @author 烟尘
 * @since 2026-02-03
 */
public interface RequestLogService extends IService<RequestLog> {

    /**
     * 记录API请求日志
     *
     * @param keyValueUsed     本次请求使用的密钥
     * @param clientIp         发起请求的客户端IP地址
     * @param responseCode     HTTP响应状态码
     * @param processingResult 处理结果摘要或错误信息
     */
    void recordRequestLog(String keyValueUsed, String clientIp, Integer responseCode, String processingResult);

}
