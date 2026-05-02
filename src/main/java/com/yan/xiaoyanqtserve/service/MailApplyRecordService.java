package com.yan.xiaoyanqtserve.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yan.xiaoyanqtserve.common.enums.ApplyStatus;
import com.yan.xiaoyanqtserve.entity.MailApplyRecord;
import org.springframework.dao.DuplicateKeyException;

/**
 * 免费领取卡密记录服务接口
 * @author yanchen
 * @since 2026/4/22
 */
public interface MailApplyRecordService extends IService<MailApplyRecord> {

    /**
     * 记录领取信息
     * <p>利用数据库唯一索引 uk_email 防止重复领取，捕获 DuplicateKeyException</p>
     *
     * @param record 领取记录实体
     * @throws DuplicateKeyException 如果该邮箱已领取过
     */
    void recordLicense(MailApplyRecord record);


    /**
     * 检查邮箱是否已验证
     *
     * @param email 邮箱地址
     * @return true-已验证，false-未验证
     */
    boolean hasVerified(String email);

    /**
     * 检查邮箱是否已发送卡密
     *
     * @param email 邮箱地址
     * @return true-已发送卡密，false-未发送卡密
     */
    boolean hasSentKey(String email);

    /**
     * 根据token查询验证记录
     *
     * @param token 验证token
     * @return 验证记录实体，不存在则返回null
     */
    MailApplyRecord getByToken(String token);

    /**
     * 更新验证记录状态
     *
     * @param id 记录ID
     * @param status 新状态
     */
    void updateStatus(Long id, ApplyStatus status);


    /**
     * 检查邮箱是否已申请但未验证（status=0）且未过期
     *
     * @param email 邮箱地址
     * @return true-已申请且未过期，false-未申请或已过期
     */
    boolean hasPendingVerification(String email);

    /**
     * 获取邮箱的待验证记录（status=0）
     *
     * @param email 邮箱地址
     * @return 验证记录实体，不存在则返回null
     */
    MailApplyRecord getPendingRecord(String email);
}
