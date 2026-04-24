package com.yan.xiaoyanqtserve.service.mail;

import com.yan.xiaoyanqtserve.common.dto.MailRecordDto;
import com.yan.xiaoyanqtserve.entity.MailFreeKeyRecord;
import com.yan.xiaoyanqtserve.service.MailFreeKeyRecordService;
import com.yan.xiaoyanqtserve.service.MailSendRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * 系统邮件发送免费卡密记录 和 申请免费卡密记录 同步更新服务
 * 描述：该服务是异步任务服务
 * @author yanchen
 * @since 2026/4/24 21:11
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MailRecordSyncService {
    private final MailSendRecordService mailSendRecordService;
    private final MailFreeKeyRecordService mailFreeKeyRecordService;


    /**
     * 同步保存邮件发送记录和免费卡密记录
     */
    @Async
    public void saveRecordAsync(MailRecordDto mailRecordDto) {
        // 保存系统邮件发送记录

        // 保存邮件申领记录
        mailFreeKeyRecordService.recordLicense(MailFreeKeyRecord.builder()
                .email(mailRecordDto.getApplicantEmail())
                .licenseKey(mailRecordDto.getLicenseKey())
                .deviceType(mailRecordDto.getDeviceType())
                .deviceId(mailRecordDto.getDeviceId())
                .clientIp(mailRecordDto.getClientIp())
                .userAgent(mailRecordDto.getUserAgent())
                .build()
        );

    }
}
