package com.yan.xiaoyanqtserve.common.utils;

import com.yan.xiaoyanqtserve.common.vo.VersionComparisonResult;

import java.util.regex.Pattern;

/**
 * 版本检查工具类
 * @author yanchen
 * @since 2026/3/11 17:48
 */
public class VersionCheckUtil {
    /**
     * 版本比较核心逻辑
     */
    public static VersionComparisonResult compareVersions(String clientVersion, String serverVersion) {
        if (clientVersion == null || serverVersion == null) {
            return VersionComparisonResult.builder()
                    .latest(false)
                    .updateRequired(true)
                    .updateLevel("unknown")
                    .message("版本信息缺失，建议更新")
                    .build();
        }

        // 去除前缀 v/V
        String cleanClient = stripVersionPrefix(clientVersion);
        String cleanServer = stripVersionPrefix(serverVersion);

        // 校验格式
        if (!isValidVersion(cleanClient) || !isValidVersion(cleanServer)) {
            return VersionComparisonResult.builder()
                    .latest(false)
                    .updateRequired(true)
                    .updateLevel("unknown")
                    .message("版本格式错误（应为 x.y.z 形式），请检查客户端版本号")
                    .build();
        }

        // 解析为整数数组
        int[] cParts = parseVersion(cleanClient);
        int[] sParts = parseVersion(cleanServer);

        int maxLen = Math.max(cParts.length, sParts.length);
        for (int i = 0; i < maxLen; i++) {
            int cVal = (i < cParts.length) ? cParts[i] : 0;
            int sVal = (i < sParts.length) ? sParts[i] : 0;

            if (sVal > cVal) {
                String level = (i == 0) ? "major" : (i == 1) ? "minor" : "patch";
                String msg = switch (level) {
                    case "major" ->
                            String.format("发现重大更新 %s（当前 %s），建议立即升级。", serverVersion, clientVersion);
                    case "minor" ->
                            String.format("发现新功能版本 %s（当前 %s），建议升级体验新特性。", serverVersion, clientVersion);
                    case "patch" ->
                            String.format("发现修复版本 %s（当前 %s），建议升级以获得稳定性提升。", serverVersion, clientVersion);
                    default -> String.format("发现新版本 %s（当前 %s），建议更新。", serverVersion, clientVersion);
                };
                return VersionComparisonResult.builder()
                        .latest(false)
                        .updateRequired(true)
                        .updateLevel(level)
                        .message(msg)
                        .build();
            } else if (sVal < cVal) {
                // 客户端版本更高（异常情况）
                return VersionComparisonResult.builder()
                        .latest(false)
                        .updateRequired(false)
                        .updateLevel("unknown")
                        .message(String.format("客户端版本 %s 高于服务器 %s，可能存在兼容风险。", clientVersion, serverVersion))
                        .build();
            }
            // 相等：继续下一位
        }

        // 完全相等
        return VersionComparisonResult.builder()
                .latest(true)
                .updateRequired(false)
                .updateLevel("none")
                .message("当前已是最新版本")
                .build();
    }

    private static String stripVersionPrefix(String version) {
        if (version != null && (version.startsWith("v") || version.startsWith("V"))) {
            return version.substring(1).trim();
        }
        return version == null ? "" : version.trim();
    }

    private static final Pattern VERSION_PATTERN = Pattern.compile("^\\d+(\\.\\d+)*$");

    private static boolean isValidVersion(String version) {
        return version != null && VERSION_PATTERN.matcher(version).matches();
    }

    private static int[] parseVersion(String versionStr) {
        if (versionStr == null || versionStr.isEmpty()) {
            return new int[0];
        }
        String[] parts = versionStr.split("\\.");
        int[] result = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            try {
                result[i] = Integer.parseInt(parts[i]);
            } catch (NumberFormatException e) {
                result[i] = 0; // 容错：非法数字视为 0
            }
        }
        return result;
    }

}
