package com.yan.xiaoyanqtserve.service.impl;

import com.yan.xiaoyanqtserve.common.dto.aigc.AigcRequestDto;
import com.yan.xiaoyanqtserve.common.dto.aigc.AigcResponseDto;
import com.yan.xiaoyanqtserve.entity.KeyInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * API密钥服务处理服务
 * @author yanchen
 * @since 2026/3/15 18:25
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApiKeyInfoService {
    private final AigcProcessingService aigcProcessingService;
    private final KeyInfoManagerService keyInfoManagerService;

    /**
     * AI处理文本，返回结果
     * @param aigcRequestDto AI处理参数
     * @return AI处理结果
     */
    public AigcResponseDto aigcDealText(AigcRequestDto aigcRequestDto, KeyInfo apiKeyUser) {
        AigcResponseDto aigcResponseDto = aigcProcessingService.dealText(aigcRequestDto.getOriginalText());

        // 减少密钥使用次数
        keyInfoManagerService.decrementKeyUses(apiKeyUser);
        return aigcResponseDto;
    }
}
