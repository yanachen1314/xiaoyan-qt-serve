package com.yan.xiaoyanqtserve.service.impl;

import com.yan.xiaoyanqtserve.entity.RequestLog;
import com.yan.xiaoyanqtserve.mapper.RequestLogMapper;
import com.yan.xiaoyanqtserve.service.RequestLogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * API请求日志表 服务实现类
 * </p>
 *
 * @author 烟尘
 * @since 2026-02-03
 */
@Service
public class RequestLogServiceImpl extends ServiceImpl<RequestLogMapper, RequestLog> implements RequestLogService {

}
