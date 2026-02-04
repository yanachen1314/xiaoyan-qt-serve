package com.yan.xiaoyanqtserve.service.impl.sys;

import com.yan.xiaoyanqtserve.entity.RequestLog;
import com.yan.xiaoyanqtserve.mapper.RequestLogMapper;
import com.yan.xiaoyanqtserve.service.RequestLogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

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
     * @param keyValueUsed     本次请求使用的密钥
     * @param clientIp         发起请求的客户端IP地址
     * @param responseCode     HTTP响应状态码
     * @param processingResult 处理结果摘要或错误信息
     */
    @Override
    public void recordRequestLog(String keyValueUsed, String clientIp, Integer responseCode, String processingResult) {
        try {
            // 创建 RequestLog 对象
            RequestLog requestLog = new RequestLog();
            requestLog.setKeyValueUsed(keyValueUsed);
            requestLog.setClientIp(clientIp);
            requestLog.setRequestTime(LocalDateTime.now());
            requestLog.setResponseCode(responseCode);
            requestLog.setProcessingResult(processingResult);

            // 保存请求日志
            save(requestLog);
        } catch (Exception e) {
            log.error("记录请求日志失败：{}", e.getMessage());
        }
    }

}
