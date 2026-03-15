package com.yan.xiaoyanqtserve.service.impl;

import com.yan.xiaoyanqtserve.common.dto.aigc.AigcRequestDto;
import com.yan.xiaoyanqtserve.common.dto.aigc.AigcResponseDto;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

/**
 * API密钥服务处理服务
 * @author yanchen
 * @since 2026/3/15 18:25
 */
@Service
public class ApiKeyInfoService {

    @Resource
    private AigcService aigcService;

    /**
     * AI处理文本，返回结果
     * @param aigcRequestDto AI处理参数
     * @return AI处理结果
     */
    public AigcResponseDto aigcDealText(AigcRequestDto aigcRequestDto) {
        AigcResponseDto aigcResponseDto = aigcService.dealText(aigcRequestDto.getOriginalText());

        // todo: 记录请求日志

        return aigcResponseDto;
    }
}
