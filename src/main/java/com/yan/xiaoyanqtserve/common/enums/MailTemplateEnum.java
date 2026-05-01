package com.yan.xiaoyanqtserve.common.enums;

import lombok.Getter;

/**
 * 邮件模板文件名称枚举
 * @author yanchen
 * @since 2025/11/29 16:41
 */
@Getter
public enum MailTemplateEnum {
    // 验证码邮件通知模板
    VERIFY_CODE("VerifyCode"),

    // 欢迎邮件通知模板
    WELCOME("Welcome"),

    // 密码重置邮件通知模板
    PWD_RESET("PwdReset"),

    // 免费授权码邮件通知模板
    FREE_LICENSE("FreeLicense"),

    // 系统邮箱授权码重置邮件通知模板
    NETEASE_AUTH_CODE_RESET("EmailAuthExpiryWarning"),

    // 邮箱验证链接邮件通知模板
    EMAIL_VERIFY_LINK("EmailVerifyLink"),

    // 邮箱验证结果-成功页面模板
    VERIFY_EMAIL_SUCCESS("VerifyEmailSuccess"),

    // 邮箱验证结果-失败页面模板
    VERIFY_EMAIL_FAILURE("VerifyEmailFailure");

    private final String name;

    MailTemplateEnum(String name) {
        this.name = name;
    }
}
