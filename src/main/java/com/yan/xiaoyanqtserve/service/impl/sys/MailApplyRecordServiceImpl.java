package com.yan.xiaoyanqtserve.service.impl.sys;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yan.xiaoyanqtserve.common.enums.ApplyStatus;
import com.yan.xiaoyanqtserve.common.exception.BizException;
import com.yan.xiaoyanqtserve.entity.MailApplyRecord;
import com.yan.xiaoyanqtserve.mapper.MailApplyRecordMapper;
import com.yan.xiaoyanqtserve.service.MailApplyRecordService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 免费领取卡密记录服务实现类
 * @author yanchen
 * @since 2026/4/22
 */
@Slf4j
@Service
public class MailApplyRecordServiceImpl extends ServiceImpl<MailApplyRecordMapper, MailApplyRecord> implements MailApplyRecordService {

    @Resource
    private MailApplyRecordMapper mailApplyRecordMapper;

    /**
     * 记录领取信息
     * <p>利用数据库唯一索引 uk_email 防止重复领取，捕获 DuplicateKeyException</p>
     *
     * @param record 领取记录实体
     * @throws DuplicateKeyException 如果该邮箱已领取过
     */
    @Override
    public void saveApplyRecord(MailApplyRecord record) {
        try {
            mailApplyRecordMapper.insert(record);
        } catch (DuplicateKeyException e) {
            throw new BizException("该邮箱已领取过");
        } catch (Exception e) {
            throw new BizException("保存领取记录失败", e);
        }
    }

    /**
     * 增加验证链接点击次数
     *
     * @param token     验证链接token
     */
    public void increaseClickCountByToken(String token) {
        mailApplyRecordMapper.increaseClickCountByToken(token, 1);
    }

    /**
     * 更新验证记录状态
     *
     * @param id 记录ID
     * @param status 新状态
     */
    @Override
    public void updateStatus(Long id, ApplyStatus status) {
        mailApplyRecordMapper.updateStatus( id, status.getCode());
    }

    /**
     * 验证领取成功，更新记录状态和 apiKey
     * @param id 记录ID
     * @param apiKey 领取的卡密
     */
    @Override
    public void verifyApplySuccess(Long id, String apiKey) {
        MailApplyRecord updateRecord = new MailApplyRecord();
        updateRecord.setId(id);
        updateRecord.setApplyApiKey(apiKey);
        updateRecord.setApplyStatus(ApplyStatus.SENT_SUCCESS.getCode());
        mailApplyRecordMapper.updateById(updateRecord);
    }

    /**
     * 验证领取失败，更新记录状态
     *
     * @param id          记录ID
     * @param applyStatus 新状态
     * @param errMsg      错误信息
     */
    @Override
    public void verifyApplyFail(Long id, ApplyStatus applyStatus, String errMsg) {
        MailApplyRecord updateRecord = new MailApplyRecord();
        updateRecord.setId(id);
        updateRecord.setApplyStatus(applyStatus.getCode());
        if (StringUtils.hasText(errMsg)) {
            updateRecord.setRemark(applyStatus.getMessage() + "，详情：" + errMsg);
        } else {
            updateRecord.setRemark(applyStatus.getMessage());
        }
        mailApplyRecordMapper.updateById(updateRecord);
    }

}
