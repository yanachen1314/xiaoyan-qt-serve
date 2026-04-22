package com.yan.xiaoyanqtserve.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yan.xiaoyanqtserve.common.Constants;
import com.yan.xiaoyanqtserve.common.utils.RedisUtil;
import com.yan.xiaoyanqtserve.entity.MailAccount;
import com.yan.xiaoyanqtserve.entity.MailSendRecord;
import com.yan.xiaoyanqtserve.mapper.MailAccountMapper;
import com.yan.xiaoyanqtserve.mapper.MailSendRecordMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * 邮件发送统计同步定时任务
 * 每日00:10将昨天的Redis缓存数据保存到数据库表中
 * 
 * @author yanchen
 * @since 2026/4/22
 */
@Slf4j
@Component
public class MailSendRecordSyncTask {

    @Resource
    private RedisUtil redisUtil;

    @Resource
    private MailAccountMapper mailAccountMapper;

    @Resource
    private MailSendRecordMapper mailSendRecordMapper;

    /**
     * 每天00:10执行，同步昨天的发送统计数据到数据库
     */
    @Scheduled(cron = "0 10 0 * * ?")
    public void syncYesterdayRecords() {
        log.info("开始同步昨天的邮件发送统计数据...");
        
        LocalDate yesterday = LocalDate.now().minusDays(1);
        List<MailAccount> accounts = mailAccountMapper.selectList(new LambdaQueryWrapper<>());
        
        int successCount = 0;
        int failCount = 0;
        
        for (MailAccount account : accounts) {
            String emailAddress = account.getEmailAddress();
            try {
                long sendCount = getCount(Constants.MAIL_SEND_COUNT_PREFIX, emailAddress, yesterday);
                long successCnt = getCount(Constants.MAIL_SEND_SUCCESS_PREFIX, emailAddress, yesterday);
                long failCnt = getCount(Constants.MAIL_SEND_FAIL_PREFIX, emailAddress, yesterday);
                
                if (sendCount > 0 || successCnt > 0 || failCnt > 0) {
                    saveOrUpdateRecord(emailAddress, yesterday, (int) sendCount, (int) successCnt, (int) failCnt);
                    successCount++;
                }
            } catch (Exception e) {
                log.error("同步邮箱 {} 的统计数据失败", emailAddress, e);
                failCount++;
            }
        }
        
        log.info("邮件发送统计数据同步完成，成功: {} 个，失败: {} 个", successCount, failCount);
    }

    /**
     * 获取Redis中的计数值
     * 
     * @param prefix       Redis Key前缀
     * @param emailAddress 邮箱地址
     * @param date         日期
     * @return 计数值
     */
    private long getCount(String prefix, String emailAddress, LocalDate date) {
        String key = prefix + date.toString() + ":" + emailAddress;
        Object countObj = redisUtil.get(key);
        if (countObj == null) {
            return 0;
        }
        try {
            return Long.parseLong(countObj.toString());
        } catch (NumberFormatException e) {
            log.warn("解析计数值失败: {}", countObj);
            return 0;
        }
    }

    /**
     * 保存或更新发送记录到数据库
     * 
     * @param emailAddress 邮箱地址
     * @param date         日期
     * @param sendCount    发送数量
     * @param successCount 成功数量
     * @param failCount    失败数量
     */
    private void saveOrUpdateRecord(String emailAddress, LocalDate date, int sendCount, int successCount, int failCount) {
        LambdaQueryWrapper<MailSendRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MailSendRecord::getEmailAddress, emailAddress);
        queryWrapper.eq(MailSendRecord::getSendDate, date);
        
        MailSendRecord existingRecord = mailSendRecordMapper.selectOne(queryWrapper);
        
        if (existingRecord != null) {
            existingRecord.setSendCount(existingRecord.getSendCount() + sendCount);
            existingRecord.setSuccessCount(existingRecord.getSuccessCount() + successCount);
            existingRecord.setFailCount(existingRecord.getFailCount() + failCount);
            mailSendRecordMapper.updateById(existingRecord);
        } else {
            MailSendRecord newRecord = new MailSendRecord();
            newRecord.setEmailAddress(emailAddress);
            newRecord.setSendDate(date);
            newRecord.setSendCount(sendCount);
            newRecord.setSuccessCount(successCount);
            newRecord.setFailCount(failCount);
            mailSendRecordMapper.insert(newRecord);
        }
        
        log.debug("同步邮箱 {} 在 {} 的统计数据: 发送={}，成功={}，失败={}", 
                emailAddress, date, sendCount, successCount, failCount);
    }
}