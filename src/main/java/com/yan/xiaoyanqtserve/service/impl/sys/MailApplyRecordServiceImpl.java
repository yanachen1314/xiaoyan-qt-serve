package com.yan.xiaoyanqtserve.service.impl.sys;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yan.xiaoyanqtserve.entity.MailApplyRecord;
import com.yan.xiaoyanqtserve.mapper.MailApplyRecordMapper;
import com.yan.xiaoyanqtserve.service.MailApplyRecordService;
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
public class MailApplyRecordServiceImpl extends ServiceImpl<MailApplyRecordMapper, MailApplyRecord> implements MailApplyRecordService {

    @Resource
    private MailApplyRecordMapper mailApplyRecordMapper;

    /**
     * 记录领取信息
     * <p>利用数据库唯一索引 uk_email 防止重复领取，捕获 DuplicateKeyException</p>
     *
     * @param record 领取记录实体
     * @throws DuplicateKeyException 如果该邮箱已领取过
     */
    @Override
    public void recordLicense(MailApplyRecord record) {
        record.setCreatedTime(LocalDateTime.now());
        mailApplyRecordMapper.insert(record);
    }

    /**
     * 检查邮箱是否已领取过免费卡密
     *
     * @param email 邮箱地址
     * @return true-已领取，false-未领取
     */
    @Override
    public boolean hasReceived(String email) {
        LambdaQueryWrapper<MailApplyRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MailApplyRecord::getApplicantEmail, email);
        return mailApplyRecordMapper.selectCount(queryWrapper) > 0;
    }


    /**
     * 检查邮箱是否已验证或已发送卡密
     *
     * @param email 邮箱地址
     * @return true-已验证或已发送卡密，false-未验证
     */
    @Override
    public boolean hasVerified(String email) {
        LambdaQueryWrapper<MailApplyRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MailApplyRecord::getApplicantEmail, email)
                    .eq(MailApplyRecord::getApplyStatus, 1);
        return mailApplyRecordMapper.selectCount(queryWrapper) > 0;
    }

    /**
     * 检查邮箱是否已发送卡密
     *
     * @param email 邮箱地址
     * @return true-已发送卡密，false-未发送卡密
     */
    @Override
    public boolean hasSentKey(String email) {
        LambdaQueryWrapper<MailApplyRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MailApplyRecord::getApplicantEmail, email)
                .eq(MailApplyRecord::getApplyStatus, 2);
        return mailApplyRecordMapper.selectCount(queryWrapper) > 0;
    }

    /**
     * 根据token查询验证记录
     *
     * @param token 验证token
     * @return 验证记录实体，不存在则返回null
     */
    @Override
    public MailApplyRecord getByToken(String token) {
        LambdaQueryWrapper<MailApplyRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MailApplyRecord::getVerifyToken, token);
        return mailApplyRecordMapper.selectOne(queryWrapper);
    }

    /**
     * 更新验证记录状态
     *
     * @param id 记录ID
     * @param status 新状态
     */
    @Override
    public void updateStatus(Long id, Integer status) {
        MailApplyRecord record = new MailApplyRecord();
        record.setId(id);
        record.setApplyStatus(status);
        mailApplyRecordMapper.updateById(record);
    }

    /**
     * 生成验证token
     * 使用UUID生成32位唯一token
     *
     * @return 验证token字符串
     */
    @Override
    public String generateVerifyToken() {
        return IdUtil.fastSimpleUUID();
    }

    /**
     * 检查邮箱是否已申请但未验证（status=0）且未过期
     *
     * @param email 邮箱地址
     * @return true-已申请且未过期，false-未申请或已过期
     */
    @Override
    public boolean hasPendingVerification(String email) {
        LambdaQueryWrapper<MailApplyRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MailApplyRecord::getApplicantEmail, email)
                    .eq(MailApplyRecord::getApplyStatus, 0)
                    .gt(MailApplyRecord::getVerifyExpireTime, LocalDateTime.now());
        return mailApplyRecordMapper.selectCount(queryWrapper) > 0;
    }

    /**
     * 获取邮箱的待验证记录（status=0）
     *
     * @param email 邮箱地址
     * @return 验证记录实体，不存在则返回null
     */
    @Override
    public MailApplyRecord getPendingRecord(String email) {
        LambdaQueryWrapper<MailApplyRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MailApplyRecord::getApplicantEmail, email)
                    .eq(MailApplyRecord::getApplyStatus, 0)
                    .orderByDesc(MailApplyRecord::getCreatedTime)
                    .last("LIMIT 1");
        return mailApplyRecordMapper.selectOne(queryWrapper);
    }
}
