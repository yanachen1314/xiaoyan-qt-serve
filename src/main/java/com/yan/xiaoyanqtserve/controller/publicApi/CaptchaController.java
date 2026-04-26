package com.yan.xiaoyanqtserve.controller.publicApi;

import com.yan.xiaoyanqtserve.common.utils.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


/**
 * 验证码相关控制器
 * @author yanchen
 * @since 2026/4/25 19:18
 */
@RestController
@RequestMapping("/api/public/tac")
@RequiredArgsConstructor
public class CaptchaController {


    /**
     * 生成验证码
     * @param type 验证码类型
     * @return 验证码数据
     */
    @PostMapping("/gen")
    public ApiResponse<?> genCaptcha(@RequestParam(value = "type", required = false)String type) {
        return ApiResponse.success();
    }

}
