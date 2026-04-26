package com.yan.xiaoyanqtserve.controller.publicApi;

import cloud.tianai.captcha.application.ImageCaptchaApplication;
import cloud.tianai.captcha.application.vo.ImageCaptchaVO;
import cloud.tianai.captcha.common.response.ApiResponse;
import cloud.tianai.captcha.validator.common.model.dto.ImageCaptchaTrack;
import com.yan.xiaoyanqtserve.common.enums.CaptchaType;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

/**
 * 验证码相关控制器
 * @author yanchen
 * @since 2026/4/25 19:18
 */
@RestController
@RequestMapping("/api/public/tac")
@RequiredArgsConstructor
public class CaptchaController {

    private final ImageCaptchaApplication imageCaptchaApplication;

    /**
     * 生成验证码
     * @param type 验证码类型
     * @return 验证码数据
     */
    @PostMapping("/gen")
    public ApiResponse<ImageCaptchaVO> genCaptcha(@RequestParam(value = "type", required = false)String type) {
        CaptchaType captchaType = CaptchaType.getByTypeName(type);
        if(captchaType == null){
            return ApiResponse.ofError("验证码类型不存在");
        }
        return imageCaptchaApplication.generateCaptcha(captchaType.name());
    }
    /**
     * 校验验证码
     * @param data 验证码数据
     * @return 校验结果
     */
    @PostMapping("/check")
    public ApiResponse<?> checkCaptcha(@RequestBody Data data) {
        ApiResponse<?> response = imageCaptchaApplication.matching(data.getId(), data.getData());
        if (response.isSuccess()) {
            return ApiResponse.ofSuccess(Collections.singletonMap("id", data.getId()));
        }
        return response;
    }

    @lombok.Data
    public static class Data {
        // 验证码id,前端回传的验证码ID
        private String  id;
        // 验证码数据,前端回传的验证码轨迹数据
        private ImageCaptchaTrack data;
    }

}
