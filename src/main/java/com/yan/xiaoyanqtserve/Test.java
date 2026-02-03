package com.yan.xiaoyanqtserve;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;

import static com.yan.xiaoyanqtserve.common.Constants.HEADER_NAME;
import static com.yan.xiaoyanqtserve.common.Constants.HEADER_PREFIX;

/**
 *
 * @author yanchen
 * @since 2026/2/3 17:55
 */
@Slf4j
public class Test {
    public static void main(String[] args) {
        log.info(":{}",extractApiKeyFromHeader("Bearer 123456"));
    }

    @Nullable
    private static  String extractApiKeyFromHeader(String header) {
        if (header != null && header.startsWith(HEADER_PREFIX)) {
            // 返回 header 值
            return header.substring(HEADER_PREFIX.length()).trim();
        }
        return null;
    }
}
