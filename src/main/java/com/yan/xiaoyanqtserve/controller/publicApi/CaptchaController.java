package com.yan.xiaoyanqtserve.controller.publicApi;

import com.yan.xiaoyanqtserve.common.enums.SceneIdEnum;
import com.yan.xiaoyanqtserve.common.utils.ApiResponse;
import com.yan.xiaoyanqtserve.service.SysConfigService;
import com.yan.xiaoyanqtserve.service.impl.captcha.AliyunCaptchaService;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;


/**
 * 【公开接口】验证码相关控制器
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

    @Resource
    private SysConfigService sysConfigService;

    /**
     * 获取验证码加密后的sceneId
     * @return 验证码加密后的sceneId
     */
    @GetMapping("/getEncryptedSceneId/{sceneId}")
    public ApiResponse<String> getEncryptedSceneId(@PathVariable String sceneId){
        SceneIdEnum bySceneId = SceneIdEnum.getBySceneId(sceneId);
        return ApiResponse.success(aliyunCaptchaService.getEncryptedSceneId(bySceneId));
    }

    /**
     * 获取验证码服务是否启用
     * @return 验证码服务是否启用
     */
    @GetMapping("/isEnabled")
    public ApiResponse<Boolean> isCaptchaEnabled() {
        return ApiResponse.success(sysConfigService.getCaptchaServiceEnabled());
    }

}
