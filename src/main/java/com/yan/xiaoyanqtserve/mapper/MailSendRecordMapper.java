package com.yan.xiaoyanqtserve.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yan.xiaoyanqtserve.entity.MailSendRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 邮件发送统计表 Mapper 接口
 * @author yanchen
 * @since 2026/4/22
 */
@Mapper
public interface MailSendRecordMapper extends BaseMapper<MailSendRecord> {
}