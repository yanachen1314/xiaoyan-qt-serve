package com.yan.xiaoyanqtserve.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yan.xiaoyanqtserve.common.Constants;
import com.yan.xiaoyanqtserve.common.dto.key.GenerateKeyDto;
import com.yan.xiaoyanqtserve.common.utils.RedisUtil;
import com.yan.xiaoyanqtserve.config.MailSafetyLimitsConfig;
import com.yan.xiaoyanqtserve.entity.MailAccount;
import com.yan.xiaoyanqtserve.entity.MailSendRecord;
import com.yan.xiaoyanqtserve.entity.ProductType;
import com.yan.xiaoyanqtserve.mapper.MailAccountMapper;
import com.yan.xiaoyanqtserve.mapper.MailSendRecordMapper;
import com.yan.xiaoyanqtserve.service.ProductTypeService;
import com.yan.xiaoyanqtserve.service.impl.KeyInfoManagerService;
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

    @Resource
    private KeyInfoManagerService keyInfoManagerService;

    @Resource
    private ProductTypeService productTypeService;

    @Resource
    private MailSafetyLimitsConfig mailSafetyLimitsConfig;

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
     * 每天00:00:10执行，重置每日免费密钥发放计数并补充密钥池
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void resetDailyFreeKeyCountAndReplenishPool() {
        log.info("开始执行每日免费密钥重置和补充任务...");
        String today = LocalDate.now().toString();

        // 1. 重置今日免费密钥发放计数
        String countKey = Constants.FREE_KEY_DAILY_COUNT + today;
        redisUtil.delete(countKey);
        log.info("已重置今日免费密钥发放计数");

        // 2. 检查密钥池剩余数量，如果小于日发送上限则补充
        Long currentPoolSize = redisUtil.sSize(Constants.FREE_KEY_POOL);
        if (currentPoolSize == null) {
            currentPoolSize = 0L;
        }
        
        Integer dailyTotalMax = mailSafetyLimitsConfig.getDailyTotalMax();
        if (currentPoolSize < dailyTotalMax) {
            log.info("密钥池剩余数量: {}，小于日发送上限: {}，开始补充200个免费密钥", currentPoolSize, dailyTotalMax);
            replenishFreeKeyPool();
        } else {
            log.info("密钥池剩余数量: {}，充足，无需补充", currentPoolSize);
        }
        
        log.info("每日免费密钥重置和补充任务执行完成");
    }

    /**
     * 补充免费密钥池
     * 使用keyInfoManagerService.generateKey()方法生成200个免费密钥并添加到Redis池中
     */
    private void replenishFreeKeyPool() {
        try {
            // 构建密钥生成参数
            GenerateKeyDto generateKeyDto = new GenerateKeyDto();
            generateKeyDto.setKeyNumber(200);
            generateKeyDto.setUsageType(Constants.FREE_KEY_USAGE_TYPE);
            generateKeyDto.setRemark("系统自动生成-免费密钥");

            ProductType productType = productTypeService.getByProductCode(Constants.FREE_KEY_PRODUCT_CODE);
            if(productType == null){
                log.warn("产品类型不存在，无法生成密钥：{}", Constants.FREE_KEY_PRODUCT_CODE);
                return;
            }
            generateKeyDto.setProductTypeId(productType.getId().intValue());

            // 调用密钥生成服务（系统定时任务使用系统用户ID 1）
            List<String> generatedKeys = keyInfoManagerService.generateKey(generateKeyDto, 1L);
            
            if (generatedKeys == null || generatedKeys.isEmpty()) {
                log.warn("密钥生成失败，未生成任何密钥");
                return;
            }
            
            log.info("成功生成 {} 个免费密钥", generatedKeys.size());
            
            // 直接将密钥值批量添加到Redis池中（generateKey返回的就是密钥值列表）
            Object[] keyValues = generatedKeys.toArray();
            Long addedCount = redisUtil.sAdd(Constants.FREE_KEY_POOL, keyValues);
            
            log.info("成功补充 {} 个免费密钥到池中", addedCount);
        } catch (Exception e) {
            log.error("补充免费密钥池失败", e);
            throw new RuntimeException("补充免费密钥池失败", e);
        }
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