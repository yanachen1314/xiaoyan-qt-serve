package com.yan.xiaoyanqtserve.service.impl.mail;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yan.xiaoyanqtserve.common.Constants;
import com.yan.xiaoyanqtserve.common.dto.key.GenerateKeyDto;
import com.yan.xiaoyanqtserve.common.exception.BizException;
import com.yan.xiaoyanqtserve.config.custome.MailPropertiesConfig;
import com.yan.xiaoyanqtserve.entity.KeyInfo;
import com.yan.xiaoyanqtserve.entity.MailApplyRecord;
import com.yan.xiaoyanqtserve.entity.MailSendRecord;
import com.yan.xiaoyanqtserve.entity.ProductType;
import com.yan.xiaoyanqtserve.mapper.MailApplyRecordMapper;
import com.yan.xiaoyanqtserve.mapper.MailSendRecordMapper;
import com.yan.xiaoyanqtserve.service.KeyInfoService;
import com.yan.xiaoyanqtserve.service.MailApplyRecordService;
import com.yan.xiaoyanqtserve.service.MailSendRecordService;
import com.yan.xiaoyanqtserve.service.ProductTypeService;
import com.yan.xiaoyanqtserve.service.impl.apikey.KeyInfoManagerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final MailPropertiesConfig mailPropertiesConfig;

    private final MailSendRecordService mailSendRecordService;
    private final MailSendRecordMapper mailSendRecordMapper;
    private final MailApplyRecordService mailApplyRecordService;
    private final MailApplyRecordMapper mailApplyRecordMapper;

    private final KeyInfoService keyInfoService;
    private final KeyInfoManagerService keyInfoManagerService;
    private final ProductTypeService productTypeService;

    /**
     * 检查邮箱是否 已申请但未发送过验证邮件或发送失败
     * 如果出现这种情况则可以重试
     * @param email 邮箱地址
     * @return true-可以重试，false-不能重试
     */
    public boolean hasEmailApply(String email) {
        // 1. 直接查询唯一申请记录（利用uk_email唯一索引）
        MailApplyRecord applyRecord = mailApplyRecordMapper.selectOne(
                new LambdaQueryWrapper<MailApplyRecord>()
                        .select(MailApplyRecord::getCreatedTime) // 仅需时间字段
                        .eq(MailApplyRecord::getApplicantEmail, email)
        );

        // 无申请记录 → 不可重试
        if (applyRecord == null) {
            return false;
        }

        // 2. 查询"本次申请后"的最新发送记录（关键：时间过滤+排序）
        MailSendRecord latestSendRecord = mailSendRecordMapper.selectOne(
                new LambdaQueryWrapper<MailSendRecord>()
                        .select(MailSendRecord::getStatus, MailSendRecord::getCreatedTime) // 仅需关键字段
                        .eq(MailSendRecord::getSendToEmail, email)
                        .ge(MailSendRecord::getCreatedTime, applyRecord.getCreatedTime()) // 申请后才有效
                        .orderByDesc(MailSendRecord::getCreatedTime) // 按发送时间倒序
                        .last("LIMIT 1") // 取最新一条
        );

        // 3. 精确判断逻辑
        if (latestSendRecord == null) {
            // 申请后无发送记录 → 可重试（首次申请场景）
            return true;
        } else if (latestSendRecord.getStatus() == 0) {
            // 申请后发送失败 → 可重试（重试场景）
            return true;
        }
        // 申请后已成功发送 → 不可重试
        return false;
    }

    /**
     * 邮箱已申请并检查是否成功发送过验证邮件
     *
     * @param email 邮箱地址
     * @return true-已申请并成功发送过验证邮件，false-已申请但未成功发送过验证邮件
     */
    public boolean hasEmailApplyAndSend(String email) {
        LambdaQueryWrapper<MailSendRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MailSendRecord::getSendToEmail, email)
                .eq(MailSendRecord::getStatus, 1);
        return mailSendRecordService.count(queryWrapper) > 0;
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
     * 说明： {@link MailApplyRecordService#increaseClickCountByToken(String)}之后进行判断，用于判断用户是否第一次点击验证链接
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
     * 同步保存apikey的数据记录，并更新申请记录状态为已发送卡密
     * @param id 申请记录ID
     * @param freeKeyValue 卡密
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveApiKeyAndUpdateApplyRecord(Long id, String freeKeyValue) {
        // 1、更新记录状态和卡密字段
        mailApplyRecordService.verifyApplySuccess(id, freeKeyValue);

        // 2、开始真正创建密钥对象的数据库记录
        try {
            // 构建密钥生成参数，生成密钥对象时需要使用
            GenerateKeyDto generateKeyDto = new GenerateKeyDto();
            generateKeyDto.setUsageType(Constants.FREE_KEY_USAGE_TYPE);
            generateKeyDto.setRemark("系统自动生成-免费密钥");

            ProductType productType = productTypeService.getByProductCode(Constants.FREE_KEY_PRODUCT_CODE);
            if (productType == null) {
                log.warn("产品类型不存在，无法生成密钥：{}", Constants.FREE_KEY_PRODUCT_CODE);
                throw new BizException("产品类型不存在！");
            }
            generateKeyDto.setProductTypeId(productType.getId().intValue());
            // 生成密钥对象
            KeyInfo keyInfo = keyInfoManagerService.buildKeyInfo(freeKeyValue, 1L, generateKeyDto, productType);
            // 设置已售出
            keyInfo.setSelled((byte) 1);
            // 保存密钥对象
            keyInfoService.save(keyInfo);
        } catch (Exception e) {
            log.error("【分发免费密钥】保存密钥信息失败，密钥值: {}", freeKeyValue);
            throw new BizException("获取卡密失败，请重新尝试！");
        }
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
    public void saveEmailSendRecordAsync(String senderEmail, String receiverEmail, Integer status, String resultMsg) {
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
