package com.yan.xiaoyanqtserve.validator;

import java.util.List;

public class EmailValidator {

    public static boolean isValidEmail(String email, List<String> emailSuffixList) {

        // 基础格式检查：必须包含且仅包含一个 '@'，且不在开头/结尾
        int atIndex = email.indexOf('@');
        if (atIndex <= 0 || atIndex != email.lastIndexOf('@') || atIndex == email.length() - 1) {
            return false;
        }

        // 分离账号和域名
        // 示例：account = "test", domain = "@example.com"
        String account = email.substring(0, atIndex);
        String domain = email.substring(atIndex).toLowerCase();

        for (String suffix : emailSuffixList) {
            if (domain.equalsIgnoreCase(suffix)) {
                return switch (suffix) {
                    case "@qq.com", "@foxmail.com" -> validateQQAccount(account);
                    case "@163.com" -> validate163Account(account);
                    case "@vip.163.com" -> validateVIP163Account(account);
                    case "@gmail.com" -> validateGmailAccount(account);
                    default -> false;
                };
            }
        }
        return false;
    }

    private static boolean validateQQAccount(String account) {
        int len = account.length();
        String accountLower = account.toLowerCase();

        // 规则1：纯数字QQ号 (5-10位)
        if (len >= 5 && len <= 10 && account.matches("\\d+")) {
            return true;
        }

        // 规则2：英文邮箱 (3-18位，以字母开头，字母/数字/下划线)
        if (len >= 3 && len <= 18) {
            if (!Character.isLetter(accountLower.charAt(0))) {
                return false; // 必须以字母开头
            }
            return accountLower.matches("[a-z0-9_]+"); // 只允许字母/数字/下划线
        }

        return false;
    }

    private static boolean validate163Account(String account) {
        int len = account.length();
        String accountLower = account.toLowerCase();

        // 规则1：手机号邮箱 (11位纯数字)
        if (len == 11 && account.matches("\\d+")) {
            return true;
        }

        // 规则2：普通邮箱 (6-18位，以字母开头，字母/数字/下划线)
        if (len >= 6 && len <= 18) {
            if (!Character.isLetter(accountLower.charAt(0))) {
                return false; // 必须以字母开头
            }
            return accountLower.matches("[a-z0-9_]+"); // 只允许字母/数字/下划线
        }

        return false;
    }

    private static boolean validateVIP163Account(String account) {
        int len = account.length();
        String accountLower = account.toLowerCase();

        // VIP邮箱规则 (5-20位，以字母开头，字母/数字/下划线)
        if (len < 5 || len > 20) {
            return false;
        }
        if (!Character.isLetter(accountLower.charAt(0))) {
            return false; // 必须以字母开头
        }
        return accountLower.matches("[a-z0-9_]+"); // 只允许字母/数字/下划线
    }

    private static boolean validateGmailAccount(String account) {
        int len = account.length();
        String accountLower = account.toLowerCase();

        // 长度检查 (6-30位原始长度)
        if (len < 6 || len > 30) {
            return false;
        }

        // 禁止以点开头/结尾
        if (accountLower.startsWith(".") || accountLower.endsWith(".")) {
            return false;
        }

        // 禁止连续点
        if (accountLower.contains("..")) {
            return false;
        }

        // 字符范围检查 (仅允许 a-z, 0-9, .)
        return accountLower.matches("[a-z0-9.]+");
    }

}