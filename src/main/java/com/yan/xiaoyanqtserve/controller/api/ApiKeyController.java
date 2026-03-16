package com.yan.xiaoyanqtserve.controller.api;

import cn.hutool.core.thread.ThreadUtil;
import com.yan.xiaoyanqtserve.common.Constants;
import com.yan.xiaoyanqtserve.common.annotaions.RateLimiter;
import com.yan.xiaoyanqtserve.common.dto.aigc.AigcRequestDto;
import com.yan.xiaoyanqtserve.common.dto.aigc.AigcResponseDto;
import com.yan.xiaoyanqtserve.common.dto.aigc.AigcResponseVo;
import com.yan.xiaoyanqtserve.common.enums.BizStatus;
import com.yan.xiaoyanqtserve.common.enums.RateLimiterType;
import com.yan.xiaoyanqtserve.common.utils.SecurityContextUtil;
import com.yan.xiaoyanqtserve.entity.KeyInfo;
import com.yan.xiaoyanqtserve.common.utils.ApiResponse;
import com.yan.xiaoyanqtserve.entity.RequestLog;
import com.yan.xiaoyanqtserve.service.impl.ApiKeyInfoService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

/**
 * API密钥服务处理接口
 * @author yanchen
 * @since 2026/2/3 16:59
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/api/key")
@RequiredArgsConstructor
public class ApiKeyController {

    private final SecurityContextUtil securityContextUtil;

    private final ApiKeyInfoService apiKeyInfoService;

    /**
     * AI处理接口
     * @param aigcRequestDto AI处理参数
     * @return 处理结果
     */
    @RateLimiter(key = "active", time = 1, count = 1, limitBy = RateLimiterType.API_KEY)
    @PostMapping("/deal/text")
    public ApiResponse<AigcResponseVo> aigcDealText(
            HttpServletRequest request, @Valid @RequestBody AigcRequestDto aigcRequestDto) {
        ThreadUtil.sleep(1200);
        KeyInfo apiKeyUser = securityContextUtil.getApiKeyUser();
        if (!securityContextUtil.isApiKeyUser() || Objects.isNull(apiKeyUser)) {
            return ApiResponse.fail(BizStatus.KEY_NOT_FOUND);
        }
        // 调用AI处理服务
        AigcResponseDto aigcDealText = apiKeyInfoService.aigcDealText(aigcRequestDto, apiKeyUser);

        // 设置请求日志
        RequestLog requestLog = (RequestLog) request.getAttribute(Constants.ATTRIBUTE_LOG_ENTITY);
        requestLog.setInputText(aigcDealText.getInputText());
        requestLog.setInputTextChars(aigcDealText.getInputChars());
        requestLog.setOutputText(aigcDealText.getOutputText());
        requestLog.setInputTokens(aigcDealText.getInputTokens());
        requestLog.setOutputTokens(aigcDealText.getOutputTokens());
        requestLog.setTotalTokens(aigcDealText.getTotalTokens());
        request.setAttribute(Constants.ATTRIBUTE_LOG_ENTITY, requestLog);

        // 返回处理结果
        return ApiResponse.success("处理成功", AigcResponseVo.builder()
                .dealResult(aigcDealText.getOutputText())
                .inputChars(aigcDealText.getInputChars())
                .build()
        );

    }

}
