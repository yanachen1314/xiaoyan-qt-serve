package com.yan.xiaoyanqtserve.service.impl.mail;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yan.xiaoyanqtserve.config.custome.MailPropertiesConfig;
import com.yan.xiaoyanqtserve.entity.MailApplyRecord;
import com.yan.xiaoyanqtserve.entity.MailSendRecord;
import com.yan.xiaoyanqtserve.mapper.MailApplyRecordMapper;
import com.yan.xiaoyanqtserve.service.MailApplyRecordService;
import com.yan.xiaoyanqtserve.service.MailSendRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

/**
 * 系统邮件发送记录管理服务
 * @author yanchen
 * @since 2026/4/24 21:11
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MailApplyManagerService {
    private final MailSendRecordService mailSendRecordService;
    private final MailPropertiesConfig mailPropertiesConfig;

    private final MailApplyRecordService mailApplyRecordService;
    private final MailApplyRecordMapper mailApplyRecordMapper;

    /**
     * 检查邮箱是否已申请但未验证（status=0）且未过期
     *
     * @param email 邮箱地址
     * @return true-已申请且未过期，false-未申请或已过期
     */
    public boolean hasPendingVerification(String email) {
        LambdaQueryWrapper<MailApplyRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MailApplyRecord::getApplicantEmail, email)
                .eq(MailApplyRecord::getApplyStatus, 0)
                .gt(MailApplyRecord::getVerifyExpireTime, LocalDateTime.now());
        return mailApplyRecordMapper.selectCount(queryWrapper) > 0;
    }
    /**
     * 检查邮箱是否已验证
     *
     * @param email 邮箱地址
     * @return true-已验证，false-未验证
     */
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
    public MailApplyRecord getByToken(String token) {
        LambdaQueryWrapper<MailApplyRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MailApplyRecord::getVerifyToken, token);
        return mailApplyRecordMapper.selectOne(queryWrapper);
    }

    /**
     * 检查是否第一次点击
     * <br/>
     * 说明：在增加点击次数{@code mailApplyRecordService.increaseClickCountByToken()}之后进行判断，用于判断用户是否第一次点击验证链接
     * @param token 验证token
     * @return true-是第一次点击，false-不是第一次点击
     */
    public Boolean isFirstClickAfterIncClickCount(String token) {
        LambdaQueryWrapper<MailApplyRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MailApplyRecord::getVerifyToken, token)
                .eq(MailApplyRecord::getClickCount, 1);
        return mailApplyRecordMapper.selectCount(queryWrapper) > 0;
    }



    /**
     * 生成验证链接token
     *
     * @return 验证token字符串
     */
    public String generateVerifyToken() {
        // 生成更安全的token
        byte[] randomBytes = new byte[mailPropertiesConfig.getVerifyLink().getTokenLength()];
        new SecureRandom().nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    /**
     * 保存邮件发送记录（异步）
     * @param senderEmail 发件人邮箱
     * @param receiverEmail 收件人邮箱
     * @param status 状态
     * @param resultMsg 结果信息
     */
    @Async("loggingExecutor")
    public void saveEmailSendRecordAsync(String senderEmail, String receiverEmail, Integer status, String resultMsg){
        try {
            MailSendRecord record = new MailSendRecord();
            record.setSystemSenderEmail(senderEmail);
            record.setSendToEmail(receiverEmail);
            record.setStatus(status);
            record.setResultMsg(resultMsg);
            record.setCreatedTime(LocalDateTime.now());
            mailSendRecordService.save(record);
        } catch (Exception e) {
            log.error("保存邮件发送记录失败，发件人：{}，收件人：{}，状态：{}，结果信息：{}", senderEmail, receiverEmail, status, resultMsg, e);
        }
    }
}
