package com.yan.xiaoyanqtserve.service.impl.mail;

import com.yan.xiaoyanqtserve.common.enums.ApplyStatus;
import com.yan.xiaoyanqtserve.config.custome.MailProperties;
import com.yan.xiaoyanqtserve.entity.MailApplyRecord;
import com.yan.xiaoyanqtserve.service.MailApplyRecordService;
import com.yan.xiaoyanqtserve.service.MailSendRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
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
    private final MailProperties mailProperties;
    /**
     * 生成验证token
     *
     * @return 验证token字符串
     */
    public String generateVerifyToken() {
        // 生成更安全的token
        byte[] randomBytes = new byte[mailProperties.getVerifyLink().getTokenLength()];
        new SecureRandom().nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    /**
     * 验证链接成功，邮件发送成功处理
     */
    public void sendMailSuccess(Long recordId, String licenseKey){
        MailApplyRecord updateRecord = new MailApplyRecord();
        updateRecord.setId(recordId);
        updateRecord.setApplyApiKey(licenseKey);
        updateRecord.setApplyStatus(ApplyStatus.HAS_SENT.getCode());
        mailApplyRecordService.updateById(updateRecord);
    }

    /**
     * 邮件发送失败后的处理
     */
    public void sendMailFailed(Long recordId, String licenseKey){
        MailApplyRecord updateRecord = new MailApplyRecord();
        updateRecord.setId(recordId);
        updateRecord.setApplyApiKey(licenseKey);
        updateRecord.setApplyStatus(ApplyStatus.HAS_SENT_FAILED.getCode());
        mailApplyRecordService.updateById(updateRecord);
    }

    /**
     * 保存邮件申领记录
     * @param record 邮件申领记录
     * @return 保存后的记录ID
     * @throws DuplicateKeyException 如果该邮箱已领取过
     */
    public Long saveApplyRecord(MailApplyRecord record) {
        return mailApplyRecordService.recordLicense(record);
    }


 /*   *//**
     * 同步保存邮件发送记录和免费卡密记录
     *//*
    @Async("loggingExecutor")
    public void saveRecordAsync(MailRecordDto mailRecordDto) {
        log.info("邮件发送记录和免费卡密记录同步保存开始 : {}", mailRecordDto);
        try {
            // 保存系统邮件发送记录
            mailSendRecordService.save(MailSendRecord.builder()
                    .systemSenderEmail(mailRecordDto.getSystemSenderEmail())
                    .sendToEmail(mailRecordDto.getApplicantEmail())
                    .status(mailRecordDto.getResultStatus())
                    .resultMsg(mailRecordDto.getResultMsg())
                    .createdTime(LocalDateTime.now())
                    .build()
            );

            // 保存邮件申领记录
            mailApplyRecordService.recordLicense(MailApplyRecord.builder()
                    .applicantEmail(mailRecordDto.getApplicantEmail())
                    .applyApiKey(mailRecordDto.getLicenseKey())
                    .deviceType(mailRecordDto.getDeviceType())
                    .deviceId(mailRecordDto.getDeviceId())
                    .clientIp(mailRecordDto.getClientIp())
                    .userAgent(mailRecordDto.getUserAgent())
                    .build()
            );
        } catch (Exception e) {
            log.error("邮件发送记录和免费卡密记录同步保存失败", e);
        }
    }*/
}
