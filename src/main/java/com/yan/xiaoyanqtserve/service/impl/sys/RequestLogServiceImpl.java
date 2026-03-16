package com.yan.xiaoyanqtserve.service.impl.sys;

import com.yan.xiaoyanqtserve.entity.RequestLog;
import com.yan.xiaoyanqtserve.mapper.RequestLogMapper;
import com.yan.xiaoyanqtserve.service.RequestLogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
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

    @Resource
    private RequestLogMapper requestLogMapper;

    /**
     * 记录API请求日志
     *
     * @param requestLog 请求日志对象，包含请求的详细信息
     */
    @Override
    public void saveModelLog(RequestLog requestLog) {
        try {
            // 保存请求日志
            save(requestLog);
        } catch (Exception e) {
            log.error("记录请求日志失败：{}", e.getMessage());
        }
    }


    /**
     * 异步保存日志
     * @param logEntry 日志对象
     */
    @Async("loggingExecutor")
    @Override
    public void saveLogAsync(RequestLog logEntry) {
        try {
            // 调用Mapper进行数据库保存
            requestLogMapper.insert(logEntry);
        } catch (Exception e) {
            // 异步方法中的异常不会传播回调用方，所以务必在这里处理
            log.error("【记录请求日志失败】记录实体：{}", logEntry);
            log.error("【记录请求日志失败】错误信息：{}", e.getMessage());
        }
    }

}
