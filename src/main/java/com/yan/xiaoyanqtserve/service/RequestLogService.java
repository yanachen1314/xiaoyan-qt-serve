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
     * 记录降AI服务请求的具体日志信息
     * <p></p>
     * 比如：输入文本、输出文本、token等
     * @param requestLog 请求日志对象，包含请求的详细信息
     */
    void saveModelLog(RequestLog requestLog);

    /**
     * 异步保存日志
     * @param logEntry 日志对象
     */
    void saveLogAsync(RequestLog logEntry);
}
