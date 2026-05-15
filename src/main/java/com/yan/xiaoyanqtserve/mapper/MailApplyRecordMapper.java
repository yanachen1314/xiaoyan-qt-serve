package com.yan.xiaoyanqtserve.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yan.xiaoyanqtserve.entity.MailApplyRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 免费领取卡密记录表 Mapper 接口
 * @author yanchen
 * @since 2026/4/22
 */
@Mapper
public interface MailApplyRecordMapper extends BaseMapper<MailApplyRecord> {

    /**
     * 增加验证链接点击次数
     *
     * @param token  邮件申请链接的token
     * @param number 点击次数
     */
    void increaseClickCountByToken(@Param("token") String token, @Param("number") Integer number);

    /**
     * 更新验证记录状态
     * @param id     记录ID
     * @param status 新状态
     * @param remark 备注
     * @param freeKeyValue 领取的卡密
     */
    void updateStatusById(@Param("id") Long id, @Param("status") Byte status, @Param("remark") String remark,
                          @Param("applyApiKey") String freeKeyValue);
    void updateStatusById(@Param("id") Long id, @Param("status") Byte status, @Param("remark") String remark);
}
