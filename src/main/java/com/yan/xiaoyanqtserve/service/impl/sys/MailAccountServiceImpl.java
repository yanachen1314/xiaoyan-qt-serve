package com.yan.xiaoyanqtserve.service.impl.sys;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yan.xiaoyanqtserve.common.dto.adminUser.MailAccountDto;
import com.yan.xiaoyanqtserve.common.dto.adminUser.MailAccountPageRequest;
import com.yan.xiaoyanqtserve.common.exception.BizException;
import com.yan.xiaoyanqtserve.entity.MailAccount;
import com.yan.xiaoyanqtserve.mapper.MailAccountMapper;
import com.yan.xiaoyanqtserve.service.MailAccountService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * <p>
 * 邮件账号配置表 服务实现类
 * </p>
 *
 * @author 烟尘
 * @since 2026-04-24
 */
@Slf4j
@Service
public class MailAccountServiceImpl extends ServiceImpl<MailAccountMapper, MailAccount> implements MailAccountService {

    @Resource
    private MailAccountMapper mailAccountMapper;

    /**
     * 分页查询邮件账户列表
     *
     * @param pageRequest 分页查询参数
     * @return 分页结果
     */
    @Override
    public IPage<MailAccount> listByPage(MailAccountPageRequest pageRequest) {
        Page<MailAccount> page = new Page<>(pageRequest.getCurrentPage(), pageRequest.getPageSize());

        LambdaQueryWrapper<MailAccount> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(pageRequest.getEmailAddress()),
                        MailAccount::getEmailAddress, pageRequest.getEmailAddress())
                .eq(pageRequest.getStatus() != null,
                        MailAccount::getStatus, pageRequest.getStatus())
                .orderByAsc(MailAccount::getPriority)
                .orderByDesc(MailAccount::getCreatedTime);

        return mailAccountMapper.selectPage(page, wrapper);
    }

    /**
     * 新增邮件账户
     *
     * @param accountDto 邮件账户信息
     */
    @Override
    public void saveAccount(MailAccountDto accountDto) {
        // 检查邮箱地址是否已存在
        LambdaQueryWrapper<MailAccount> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MailAccount::getEmailAddress, accountDto.getEmailAddress());
        Long count = mailAccountMapper.selectCount(wrapper);
        if (count > 0) {
            throw new BizException("邮箱地址已存在");
        }

        MailAccount mailAccount = new MailAccount();
        BeanUtils.copyProperties(accountDto, mailAccount);
        mailAccountMapper.insert(mailAccount);
    }

    /**
     * 更新邮件账户
     *
     * @param accountDto 邮件账户信息
     */
    @Override
    public void updateAccount(MailAccountDto accountDto) {
        if (accountDto.getId() == null) {
            throw new BizException("参数错误");
        }

        // 检查邮箱地址是否被其他账户使用
        LambdaQueryWrapper<MailAccount> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MailAccount::getEmailAddress, accountDto.getEmailAddress())
                .ne(MailAccount::getId, accountDto.getId());
        Long count = mailAccountMapper.selectCount(wrapper);
        if (count > 0) {
            throw new BizException("邮箱地址已被其他账户使用");
        }

        MailAccount mailAccount = new MailAccount();
        BeanUtils.copyProperties(accountDto, mailAccount);
        mailAccountMapper.updateById(mailAccount);
    }

    /**
     * 删除邮件账户
     *
     * @param id 账户 ID
     */
    @Override
    public void deleteAccount(Long id) {
        if (id == null) {
            throw new BizException("参数错误");
        }
        mailAccountMapper.deleteById(id);
    }
}
