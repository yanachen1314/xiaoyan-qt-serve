package com.yan.xiaoyanqtserve.service.impl.sys;

import com.yan.xiaoyanqtserve.entity.RequestLog;
import com.yan.xiaoyanqtserve.mapper.RequestLogMapper;
import com.yan.xiaoyanqtserve.service.RequestLogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * <p>
 * API请求日志表 服务实现类
 * </p>
 *
 * @author 烟尘
 * @since 2026-02-03
 */
@Slf4j
@Service
public class RequestLogServiceImpl extends ServiceImpl<RequestLogMapper, RequestLog> implements RequestLogService {

    /**
     * 记录API请求日志
     *
     * @param requestLog 请求日志对象，包含请求的详细信息
     */
    @Override
    public void recordLog(RequestLog requestLog) {
        try {
            // 保存请求日志
            save(requestLog);
        } catch (Exception e) {
            log.error("记录请求日志失败：{}", e.getMessage());
        }
    }

}
