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
    void saveApplyRecord(MailApplyRecord record);

    /**
     * 增加验证链接点击次数
     * @param token     验证链接token
     */
    void increaseClickCountByToken(String token);


    /**
     * 更新验证记录状态
     *
     * @param id 记录ID
     * @param status 新状态
     */
    void updateStatus(Long id, ApplyStatus status);

    /**
     * 验证领取成功，更新记录状态和 apiKey
     * @param id 记录ID
     * @param apiKey 领取的卡密
     */
    void verifyApplySuccess(Long id, String apiKey);

    /**
     * 验证领取失败，更新记录状态
     *
     * @param id          记录ID
     * @param applyStatus 新状态
     * @param errMsg      错误信息
     */
    void verifyApplyFail(Long id, ApplyStatus applyStatus, String errMsg);
}
