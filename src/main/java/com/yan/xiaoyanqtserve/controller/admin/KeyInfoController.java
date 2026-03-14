package com.yan.xiaoyanqtserve.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yan.xiaoyanqtserve.common.dto.key.GenerateKeyDto;
import com.yan.xiaoyanqtserve.common.utils.ApiResponse;
import com.yan.xiaoyanqtserve.entity.KeyInfo;
import com.yan.xiaoyanqtserve.service.KeyInfoService;
import com.yan.xiaoyanqtserve.service.impl.KeyInfoManagerService;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 密钥信息表 前端控制器
 *
 * @author 烟尘
 * @since 2026-02-03
 */
@Validated
@RestController
@RequestMapping("/api/admin/keyInfo")
public class KeyInfoController {
    @Resource
    private KeyInfoService keyInfoService;
    @Resource
    private KeyInfoManagerService keyInfoManagerService;

    /**
     * 密钥生成
     * @param generateKeyDto 密钥生成参数
     * @return 密钥生成结果
     */
    @PostMapping("/generate")
    public ApiResponse<?> generate(@Valid @RequestBody GenerateKeyDto generateKeyDto) {
        keyInfoManagerService.generateKey(generateKeyDto);
        return ApiResponse.success();
    }

    /**
     * 分页查询密钥列表
     * @param page 页码
     * @param pageSize 每页大小
     * @param status 密钥状态（可选）
     * @param activated 激活状态（可选）
     * @param productType 产品类型（可选）
     * @return 分页结果
     */
    @GetMapping("/list")
    public ApiResponse<Page<KeyInfo>> getList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) Byte status,
            @RequestParam(required = false) Byte activated,
            @RequestParam(required = false) String productType
    ) {
        Page<KeyInfo> keyInfoPage = keyInfoService.getList(page, pageSize, status, activated, productType);
        return ApiResponse.success(keyInfoPage);
    }
}
