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
     * @param requestLog 请求日志对象，包含请求的详细信息
     */
    void recordLog(RequestLog requestLog);

}
