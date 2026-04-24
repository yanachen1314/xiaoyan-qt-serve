package com.yan.xiaoyanqtserve.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yan.xiaoyanqtserve.entity.MailSendRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 邮件发送统计表 Mapper 接口
 * </p>
 *
 * @author 烟尘
 * @since 2026-04-24
 */
@Mapper
public interface MailSendRecordMapper extends BaseMapper<MailSendRecord> {

}
