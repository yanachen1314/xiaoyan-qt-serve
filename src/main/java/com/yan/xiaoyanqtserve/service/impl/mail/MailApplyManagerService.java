package com.yan.xiaoyanqtserve.service.impl.mail;

import com.yan.xiaoyanqtserve.common.enums.ApplyStatus;
import com.yan.xiaoyanqtserve.config.custome.MailPropertiesConfig;
import com.yan.xiaoyanqtserve.entity.MailApplyRecord;
import com.yan.xiaoyanqtserve.entity.MailSendRecord;
import com.yan.xiaoyanqtserve.service.MailApplyRecordService;
import com.yan.xiaoyanqtserve.service.MailSendRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
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
    private final MailApplyRecordService mailApplyRecordService;
    private final MailPropertiesConfig mailPropertiesConfig;
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
     * 验证链接成功，更新申请记录状态
     */
    public void updateApplyRecordStatusSuccess(Long recordId, String licenseKey){
        MailApplyRecord updateRecord = new MailApplyRecord();
        updateRecord.setId(recordId);
        updateRecord.setApplyApiKey(licenseKey);
        updateRecord.setApplyStatus(ApplyStatus.HAS_SENT.getCode());
        mailApplyRecordService.updateById(updateRecord);
    }

    /**
     * 验证链接失败，更新申请记录状态
     */
    public void updateApplyRecordStatusFailed(Long recordId,  ApplyStatus applyStatus){
        MailApplyRecord updateRecord = new MailApplyRecord();
        updateRecord.setId(recordId);
        updateRecord.setApplyStatus(applyStatus.getCode());
        updateRecord.setRemark(applyStatus.getMessage());
        mailApplyRecordService.updateById(updateRecord);
    }

    /**
     * 保存邮件申领记录（同步）
     * @param record 邮件申领记录
     * @throws DuplicateKeyException 如果该邮箱已领取过
     */
    public void saveApplyRecordSync(MailApplyRecord record) {
        mailApplyRecordService.recordLicense(record);
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
