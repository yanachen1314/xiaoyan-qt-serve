package com.yan.xiaoyanqtserve.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yan.xiaoyanqtserve.common.dto.adminUser.MailAccountDto;
import com.yan.xiaoyanqtserve.common.dto.adminUser.MailAccountPageRequest;
import com.yan.xiaoyanqtserve.common.utils.ApiResponse;
import com.yan.xiaoyanqtserve.entity.MailAccount;
import com.yan.xiaoyanqtserve.service.MailAccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 邮件账号配置表 前端控制器
 * </p>
 *
 * @author 烟尘
 * @since 2026-04-24
 */
@RestController
@RequestMapping("/api/admin/mailAccount")
@RequiredArgsConstructor
public class MailAccountController {

    private final MailAccountService mailAccountService;

    /**
     * 分页查询邮件账户列表
     *
     * @param pageRequest 分页查询参数
     * @return 分页结果
     */
    @PostMapping("/list")
    public ApiResponse<IPage<MailAccount>> list(@RequestBody MailAccountPageRequest pageRequest) {
        return ApiResponse.success(mailAccountService.listByPage(pageRequest));
    }

    /**
     * 新增邮件账户
     *
     * @param accountDto 邮件账户信息
     * @return 操作结果
     */
    @PostMapping("/add")
    public ApiResponse<String> add(@RequestBody @Valid MailAccountDto accountDto) {
        mailAccountService.saveAccount(accountDto);
        return ApiResponse.success("新增成功", null);
    }

    /**
     * 更新邮件账户
     *
     * @param accountDto 邮件账户信息
     * @return 操作结果
     */
    @PostMapping("/update")
    public ApiResponse<String> update(@RequestBody @Valid MailAccountDto accountDto) {
        mailAccountService.updateAccount(accountDto);
        return ApiResponse.success("更新成功", null);
    }

    /**
     * 删除邮件账户
     *
     * @param id 账户 ID
     * @return 操作结果
     */
    @PostMapping("/delete/{id}")
    public ApiResponse<String> delete(@PathVariable Long id) {
        mailAccountService.deleteAccount(id);
        return ApiResponse.success("删除成功", null);
    }
}
