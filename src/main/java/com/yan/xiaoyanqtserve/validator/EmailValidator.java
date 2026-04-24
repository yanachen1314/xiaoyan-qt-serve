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
                    case "qq.com", "foxmail.com" -> validateQQAccount(account);
                    case "163.com" -> validate163Account(account);
                    case "vip.163.com" -> validateVIP163Account(account);
                    case "gmail.com" -> validateGmailAccount(account);
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

    /**
     * 测试邮箱验证器功能
     * 
     * @param args 命令行参数（未使用）
     */
    public static void main(String[] args) {
        // 定义支持的后缀列表
        List<String> emailSuffixList = List.of("@qq.com", "@foxmail.com", "@163.com", "@vip.163.com", "@gmail.com");

        System.out.println("========== QQ邮箱测试 ==========");
        // 有效的QQ邮箱 - 纯数字QQ号（5-10位）
        runTest("12345@qq.com", emailSuffixList, true);   // 5位QQ号
        runTest("1234567890@qq.com", emailSuffixList, true); // 10位QQ号
        runTest("1234@qq.com", emailSuffixList, false);  // 4位太短
        runTest("12345678901@qq.com", emailSuffixList, false); // 11位太长

        // 有效的QQ邮箱 - 字母账号（3-18位，字母开头）
        runTest("abc@qq.com", emailSuffixList, true);    // 3位字母
        runTest("test_user@qq.com", emailSuffixList, true); // 带下划线
        runTest("user123@qq.com", emailSuffixList, true); // 字母数字组合
        runTest("1abc@qq.com", emailSuffixList, false);  // 数字开头无效
        runTest("ab@qq.com", emailSuffixList, false);    // 2位太短

        System.out.println("\n========== Foxmail邮箱测试 ==========");
        runTest("test@foxmail.com", emailSuffixList, true);
        runTest("user123@foxmail.com", emailSuffixList, true);
        runTest("12345@foxmail.com", emailSuffixList, true); // QQ数字格式

        System.out.println("\n========== 163邮箱测试 ==========");
        // 有效的163邮箱 - 手机号格式（11位）
        runTest("13800138000@163.com", emailSuffixList, true);
        runTest("12345678901@163.com", emailSuffixList, true);
        runTest("1234567890@163.com", emailSuffixList, false); // 10位不是手机号

        // 有效的163邮箱 - 普通邮箱格式（6-18位）
        runTest("testuser@163.com", emailSuffixList, true);
        runTest("test_user@163.com", emailSuffixList, true);
        runTest("test123@163.com", emailSuffixList, true);
        runTest("abcde@163.com", emailSuffixList, false);  // 5位太短
        runTest("1testuser@163.com", emailSuffixList, false); // 数字开头无效

        System.out.println("\n========== VIP.163邮箱测试 ==========");
        runTest("testuser@vip.163.com", emailSuffixList, true);
        runTest("test_user123@vip.163.com", emailSuffixList, true);
        runTest("test@vip.163.com", emailSuffixList, false); // 4位太短（需要5-20位）
        runTest("1testuser@vip.163.com", emailSuffixList, false); // 数字开头无效

        System.out.println("\n========== Gmail邮箱测试 ==========");
        runTest("testuser@gmail.com", emailSuffixList, true);
        runTest("test.user@gmail.com", emailSuffixList, true);
        runTest("t.e.s.t@gmail.com", emailSuffixList, true);
        runTest("testuser123@gmail.com", emailSuffixList, true);
        runTest(".test@gmail.com", emailSuffixList, false); // 以点开头无效
        runTest("test.@gmail.com", emailSuffixList, false); // 以点结尾无效
        runTest("te..st@gmail.com", emailSuffixList, false); // 连续点无效
        runTest("test@gmail.com", emailSuffixList, false);  // 4位太短（需要6-30位）
        runTest("test_user@gmail.com", emailSuffixList, false); // 下划线在Gmail无效

        System.out.println("\n========== 基础格式测试 ==========");
        runTest("@qq.com", emailSuffixList, false);        // 账号为空
        runTest("test", emailSuffixList, false);           // 缺少@
        runTest("test@qq", emailSuffixList, false);        // 后缀不匹配
        runTest("test@@qq.com", emailSuffixList, false);   // 多个@
        runTest("test@qq.com@", emailSuffixList, false);   // @在结尾
        runTest("TEST@QQ.COM", emailSuffixList, true);     // 大写应正常匹配
        runTest("Test.User@Gmail.Com", emailSuffixList, true); // 混合大写

        System.out.println("\n========== 不支持的后缀测试 ==========");
        runTest("test@hotmail.com", emailSuffixList, false);
        runTest("test@outlook.com", emailSuffixList, false);
        runTest("test@yahoo.com", emailSuffixList, false);

        System.out.println("\n========== 测试完成 ==========");
    }

    /**
     * 执行单个测试用例并输出结果
     * 
     * @param email 待测试的邮箱地址
     * @param emailSuffixList 支持的邮箱后缀列表
     * @param expected 期望的验证结果（true表示有效，false表示无效）
     */
    private static void runTest(String email, List<String> emailSuffixList, boolean expected) {
        boolean actual = isValidEmail(email, emailSuffixList);
        String status = (actual == expected) ? "✓ PASS" : "✗ FAIL";
        System.out.println(String.format("[%s] 邮箱: %-30s 期望: %-5s 实际: %-5s",
                status, email, expected, actual));
    }
}