package com.yan.xiaoyanqtserve.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yan.xiaoyanqtserve.common.dto.key.GenerateKeyDto;
import com.yan.xiaoyanqtserve.common.dto.key.KeyInfoPageRequest;
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
     * @param pageRequest 分页查询参数
     * @return 分页结果
     */
    @PostMapping("/list")
    public ApiResponse<Page<KeyInfo>> list(@Valid @RequestBody KeyInfoPageRequest pageRequest) {
        Page<KeyInfo> keyInfoPage = keyInfoService.getList(pageRequest);
        return ApiResponse.success(keyInfoPage);
    }
}
