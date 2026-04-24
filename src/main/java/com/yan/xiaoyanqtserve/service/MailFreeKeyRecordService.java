package com.yan.xiaoyanqtserve.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yan.xiaoyanqtserve.entity.MailFreeKeyRecord;
import org.springframework.dao.DuplicateKeyException;

/**
 * 免费领取卡密记录服务接口
 * @author yanchen
 * @since 2026/4/22
 */
public interface MailFreeKeyRecordService extends IService<MailFreeKeyRecord> {

    /**
     * 检查邮箱是否已领取过免费卡密
     *
     * @param email 邮箱地址
     * @return true-已领取，false-未领取
     */
    boolean hasReceived(String email);

    /**
     * 记录领取信息
     * <p>利用数据库唯一索引 uk_email 防止重复领取，捕获 DuplicateKeyException</p>
     *
     * @param record 领取记录实体
     * @throws DuplicateKeyException 如果该邮箱已领取过
     */
    void recordLicense(MailFreeKeyRecord record);
}
