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
     * 获取验证码加密后的sceneId: "f1yi7e7j"
     * @return 验证码加密后的sceneId
     */
    @GetMapping("/getEncryptedSceneId/{sceneId}")
    public ApiResponse<String> getEncryptedSceneId(@PathVariable String sceneId){
        return ApiResponse.success(aliyunCaptchaService.getEncryptedSceneId(sceneId));
    }

    /**
     * 校验验证码
     * @return 验证码数据
     */
    @PostMapping("/verify")
    public ApiResponse<CaptchaVerifyVo> verifyAliyunCaptcha(@RequestBody CaptchaVerifyDto captchaVerifyDto) {
        boolean captchaResult = aliyunCaptchaService.verifyCaptcha("f1yi7e7j", captchaVerifyDto.getCaptchaVerifyParam());
        // 验证码校验失败
        if(!captchaResult){
            return ApiResponse.success(CaptchaVerifyVo.builder()
                    .captchaResult(false)
                    .bizResult(false)
                    .bizData(null)
                    .build());
        }
        
        // 校验正常，执行业务逻辑
        return ApiResponse.success(CaptchaVerifyVo.builder()
                .captchaResult(true)
                .bizResult(true)
                .bizData("验证码校验成功bizData")
                .build());
    }

    @Data
    @Builder
    public static class CaptchaVerifyVo {
        // 验证码结果
        private Boolean captchaResult;
        // 业务结果
        private Boolean bizResult;
        private Object bizData;
    }
}
