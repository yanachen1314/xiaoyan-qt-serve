package com.yan.xiaoyanqtserve.controller.admin;

import com.yan.xiaoyanqtserve.common.dto.key.GenerateKeyDto;
import com.yan.xiaoyanqtserve.service.KeyInfoService;
import com.yan.xiaoyanqtserve.common.utils.ApiResponse;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 密钥信息表 前端控制器
 *
 * @author 烟尘
 * @since 2026-02-03
 */
@Validated
@RestController
@RequestMapping("/admin/keyInfo")
public class KeyInfoController {
    @Resource
    private KeyInfoService keyInfoService;

    /**
     * 密钥生成
     * @param generateKeyDto 密钥生成参数
     * @return 密钥生成结果
     */
    @PostMapping("/generate")
    public ApiResponse<?> generate(@Valid @RequestBody GenerateKeyDto generateKeyDto) {
        keyInfoService.generateKey(generateKeyDto);
        return ApiResponse.success();
    }
}
