package com.yan.xiaoyanqtserve.service.impl;

import com.yan.xiaoyanqtserve.entity.KeyInfo;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 *
 * @author yanchen
 * @since 2026/2/3 18:18
 */
@Service
public class RequestProcessingService {
    /**
     * 验证 API Key
     * @param apiKey API Key
     * @return 校验通过返回KeyInfo
     */
    public Optional<KeyInfo> validateKey(String apiKey) {
        return null;
    }
}
