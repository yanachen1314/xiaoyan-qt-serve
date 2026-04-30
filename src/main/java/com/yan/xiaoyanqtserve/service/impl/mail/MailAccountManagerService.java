package com.yan.xiaoyanqtserve.service.impl.mail;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yan.xiaoyanqtserve.common.Constants;
import com.yan.xiaoyanqtserve.common.exception.BizException;
import com.yan.xiaoyanqtserve.config.MailSafetyLimitsConfig;
import com.yan.xiaoyanqtserve.entity.MailAccount;
import com.yan.xiaoyanqtserve.mapper.MailAccountMapper;
import com.yan.xiaoyanqtserve.common.utils.RedisUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * 邮件账号管理服务
 * 实现轮询策略选择可用邮箱账号，并管理发送统计
 * 
 * @author yanchen
 * @since 2026/4/22
 */
@Slf4j
@Service
public class MailAccountManagerService {

    @Resource
    private MailAccountMapper mailAccountMapper;
    @Resource
    private RedisUtil redisUtil;
    @Resource
    private MailSafetyLimitsConfig mailSafetyLimitsConfig;

    /**
     * 获取所有启用的邮箱账号
     * 
     * @return 启用的邮箱账号列表
     */
    public List<MailAccount> getEnabledAccounts() {
        LambdaQueryWrapper<MailAccount> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MailAccount::getStatus, 1);
        queryWrapper.orderByAsc(MailAccount::getPriority);
        return mailAccountMapper.selectList(queryWrapper);
    }

    /**
     * 获取可用的邮箱账号列表（状态为启用且未达每日上限）
     * 
     * @return 可用邮箱账号列表
     */
    public List<MailAccount> getAvailableAccounts() {
        List<MailAccount> enabledAccounts = getEnabledAccounts();
        LocalDate today = LocalDate.now();
        
        return enabledAccounts.stream()
                .filter(account -> !isAccountOverLimit(account, today))
                .toList();
    }

    /**
     * 使用轮询策略选择一个可用的邮箱账号
     * 
     * @return 选中的邮箱账号
     * @throws BizException 如果没有可用账号
     */
    public MailAccount selectAccountByRoundRobin() {
        List<MailAccount> availableAccounts = getAvailableAccounts();
        if (availableAccounts.isEmpty()) {
            throw new BizException("没有可用的邮箱账号");
        }
        
        // 使用Redis原子操作实现轮询
        Long index = redisUtil.incr(Constants.MAIL_ACCOUNT_ROUND_ROBIN_KEY, 1);
        if (index < 0) {
            index = 0L;
        }
        
        // 对可用账号数取模
        int accountIndex = (int) (index % availableAccounts.size());
        return availableAccounts.get(accountIndex);
    }

    /**
     * 判断今日免费密钥发放是否已达上限
     * @return true 表示已达上限，false 表示未达上限
     */
    public boolean isTodayFreeKeyLimitReached() {
        String today = LocalDate.now().toString();
        String countKey = Constants.FREE_KEY_DAILY_COUNT + today;

        Object countObj = redisUtil.get(countKey);
        if (countObj == null) {
            return false;
        }

        long todayCount = Long.parseLong(countObj.toString());
        return todayCount >= mailSafetyLimitsConfig.getDailyTotalMax();
    }
    /**
     * 检查账号是否达到每日发送上限
     *
     * @param account 邮箱账号
     * @param date    日期
     * @return true-已达上限, false-未达上限
     */
    public boolean isAccountOverLimit(MailAccount account, LocalDate date) {
        String countKey = Constants.MAIL_SEND_COUNT_PREFIX + date.toString() + ":" + account.getEmailAddress();
        Object countObj = redisUtil.get(countKey);

        if (countObj == null) {
            return false;
        }

        long currentCount = parseCount(countObj);
        return currentCount >= account.getDailyLimit();
    }


    /**
     * 记录发送成功统计到Redis
     * 
     * @param emailAddress 邮箱地址
     * @param date         日期
     */
    public void recordSuccess(String emailAddress, LocalDate date) {
        String countKey = Constants.MAIL_SEND_COUNT_PREFIX + date.toString() + ":" + emailAddress;
        String successKey = Constants.MAIL_SEND_SUCCESS_PREFIX + date.toString() + ":" + emailAddress;
        
        redisUtil.incr(countKey, 1);
        redisUtil.incr(successKey, 1);
    }

    /**
     * 记录发送失败统计到Redis
     * 
     * @param emailAddress 邮箱地址
     * @param date         日期
     */
    public void recordFailure(String emailAddress, LocalDate date) {
        String countKey = Constants.MAIL_SEND_COUNT_PREFIX + date.toString() + ":" + emailAddress;
        String failKey = Constants.MAIL_SEND_FAIL_PREFIX + date.toString() + ":" + emailAddress;
        
        redisUtil.incr(countKey, 1);
        redisUtil.incr(failKey, 1);
    }

    /**
     * 获取账号当天的发送数量
     *
     * @param emailAddress 邮箱地址
     * @param date         日期
     * @return 发送数量
     */
    public long getSendCount(String emailAddress, LocalDate date) {
        String countKey = Constants.MAIL_SEND_COUNT_PREFIX + date.toString() + ":" + emailAddress;
        Object countObj = redisUtil.get(countKey);
        if (countObj == null) {
            return 0;
        }
        return parseCount(countObj);
    }

    /**
     * 获取账号当天的成功发送数量
     * 
     * @param emailAddress 邮箱地址
     * @param date         日期
     * @return 成功发送数量
     */
    public long getSendSuccessCount(String emailAddress, LocalDate date) {
        String successKey = Constants.MAIL_SEND_SUCCESS_PREFIX + date.toString() + ":" + emailAddress;
        Object countObj = redisUtil.get(successKey);
        if (countObj == null) {
            return 0;
        }
        return parseCount(countObj);
    }

    /**
     * 获取账号当天的失败发送数量
     * 
     * @param emailAddress 邮箱地址
     * @param date         日期
     * @return 失败发送数量
     */
    public long getSendFailCount(String emailAddress, LocalDate date) {
        String failKey = Constants.MAIL_SEND_FAIL_PREFIX + date.toString() + ":" + emailAddress;
        Object countObj = redisUtil.get(failKey);
        if (countObj == null) {
            return 0;
        }
        return parseCount(countObj);
    }

    /**
     * 解析发送数量
     * 
     * @param countObj Redis中的计数值
     * @return 发送数量
     */
    private long parseCount(Object countObj) {
        if (countObj instanceof Number) {
            return ((Number) countObj).longValue();
        }
        try {
            return Long.parseLong(countObj.toString());
        } catch (NumberFormatException e) {
            log.warn("解析发送数量失败: {}", countObj);
            return 0;
        }
    }
}