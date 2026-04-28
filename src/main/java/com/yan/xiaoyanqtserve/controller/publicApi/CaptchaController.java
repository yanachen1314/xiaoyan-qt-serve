package com.yan.xiaoyanqtserve.controller.publicApi;

import com.yan.xiaoyanqtserve.common.dto.CaptchaVerifyDto;
import com.yan.xiaoyanqtserve.common.utils.ApiResponse;
import com.yan.xiaoyanqtserve.service.captcha.AliyunCaptchaService;
import jakarta.annotation.Resource;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


/**
 * 验证码相关控制器
 * @author yanchen
 * @since 2026/4/25 19:18
 */
@Slf4j
@RestController
@RequestMapping("/api/public/captcha")
@RequiredArgsConstructor
public class CaptchaController {

    @Resource
    private AliyunCaptchaService aliyunCaptchaService;

    /**
     * 校验验证码
     * @return 验证码数据
     */
    @PostMapping("/verify")
    public ApiResponse<CaptchaVerifyVo> verifyAliyunCaptcha(@RequestBody CaptchaVerifyDto captchaVerifyDto) {
        boolean captchaResult = aliyunCaptchaService.verifyCaptcha("f1yi7e7j", captchaVerifyDto.getCaptchaVerifyParam());
        return ApiResponse.success(CaptchaVerifyVo.builder()
                .captchaResult(captchaResult)
                .bizResult(Map.of(
                        "status",true,
                        "data","验证码校验成功bizData"))
                .build());
    }


    @Data
    @Builder
    public static class CaptchaVerifyVo {
        // 验证码结果
        private Boolean captchaResult;
        // 业务结果
        private Object bizResult;
    }
}
