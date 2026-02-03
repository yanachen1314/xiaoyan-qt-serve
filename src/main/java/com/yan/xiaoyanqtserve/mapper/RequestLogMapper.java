package com.yan.xiaoyanqtserve.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yan.xiaoyanqtserve.entity.RequestLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * API请求日志表 Mapper 接口
 * </p>
 *
 * @author 烟尘
 * @since 2026-02-03
 */
@Mapper
public interface RequestLogMapper extends BaseMapper<RequestLog> {

}
