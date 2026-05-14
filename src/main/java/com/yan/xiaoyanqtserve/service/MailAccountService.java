package com.yan.xiaoyanqtserve.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yan.xiaoyanqtserve.common.dto.adminUser.MailAccountDto;
import com.yan.xiaoyanqtserve.common.dto.adminUser.MailAccountPageRequest;
import com.yan.xiaoyanqtserve.entity.MailAccount;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 邮件账号配置表 服务类
 * </p>
 *
 * @author 烟尘
 * @since 2026-04-25
 */
public interface MailAccountService extends IService<MailAccount> {

    /**
     * 分页查询邮件账户列表
     *
     * @param pageRequest 分页查询参数
     * @return 分页结果
     */
    IPage<MailAccount> listByPage(MailAccountPageRequest pageRequest);

    /**
     * 新增邮件账户
     *
     * @param accountDto 邮件账户信息
     */
    void saveAccount(MailAccountDto accountDto);

    /**
     * 更新邮件账户
     *
     * @param accountDto 邮件账户信息
     */
    void updateAccount(MailAccountDto accountDto);

    /**
     * 删除邮件账户
     *
     * @param id 账户 ID
     */
    void deleteAccount(Long id);
}
