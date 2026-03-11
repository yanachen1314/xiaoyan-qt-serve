package com.yan.xiaoyanqtserve.controller.api;

import com.yan.xiaoyanqtserve.common.dto.aigc.AigcRequestDto;
import com.yan.xiaoyanqtserve.common.enums.BizStatus;
import com.yan.xiaoyanqtserve.common.utils.SecurityContextUtil;
import com.yan.xiaoyanqtserve.entity.KeyInfo;
import com.yan.xiaoyanqtserve.common.utils.ApiResponse;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Objects;

/**
 * API密钥处理接口
 * @author yanchen
 * @since 2026/2/3 16:59
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/api")
public class ApiKeyController {

    @Resource
    private SecurityContextUtil securityContextUtil;

    /**
     * AI处理接口
     * @param aigcRequestDto AI处理参数
     * @return 处理结果
     */
    @PostMapping("/deal/text")
    public ApiResponse<?> aiDeal(@Valid @RequestBody AigcRequestDto aigcRequestDto) {
        KeyInfo apiKeyUser = securityContextUtil.getApiKeyUser();
        if(!securityContextUtil.isApiKeyUser() || Objects.isNull(apiKeyUser)){
            return ApiResponse.fail(BizStatus.KEY_NOT_FOUND);
        }

        // 返回处理结果
        return ApiResponse.success("处理成功", Map.of("processedContent", 123));
    }
}
