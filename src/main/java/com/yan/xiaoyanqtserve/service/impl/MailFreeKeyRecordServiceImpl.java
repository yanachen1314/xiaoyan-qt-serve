package com.yan.xiaoyanqtserve.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yan.xiaoyanqtserve.entity.MailFreeKeyRecord;
import com.yan.xiaoyanqtserve.mapper.MailFreeKeyRecordMapper;
import com.yan.xiaoyanqtserve.service.MailFreeKeyRecordService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 免费领取卡密记录服务实现类
 * @author yanchen
 * @since 2026/4/22
 */
@Slf4j
@Service
public class MailFreeKeyRecordServiceImpl extends ServiceImpl<MailFreeKeyRecordMapper, MailFreeKeyRecord> implements MailFreeKeyRecordService {

    @Resource
    private MailFreeKeyRecordMapper mailFreeKeyRecordMapper;

    /**
     * 检查邮箱是否已领取过免费卡密
     *
     * @param email 邮箱地址
     * @return true-已领取，false-未领取
     */
    @Override
    public boolean hasReceived(String email) {
        LambdaQueryWrapper<MailFreeKeyRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MailFreeKeyRecord::getEmail, email);
        return mailFreeKeyRecordMapper.selectCount(queryWrapper) > 0;
    }

    /**
     * 记录领取信息
     * <p>利用数据库唯一索引 uk_email 防止重复领取，捕获 DuplicateKeyException</p>
     *
     * @param record 领取记录实体
     * @throws DuplicateKeyException 如果该邮箱已领取过
     */
    @Override
    public void recordLicense(MailFreeKeyRecord record) {
        record.setCreatedTime(LocalDateTime.now());
        mailFreeKeyRecordMapper.insert(record);
    }
}
