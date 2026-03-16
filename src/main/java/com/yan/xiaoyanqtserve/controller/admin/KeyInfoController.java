package com.yan.xiaoyanqtserve.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yan.xiaoyanqtserve.common.dto.key.GenerateKeyDto;
import com.yan.xiaoyanqtserve.common.dto.key.KeyInfoPageRequest;
import com.yan.xiaoyanqtserve.common.utils.ApiResponse;
import com.yan.xiaoyanqtserve.common.utils.SecurityContextUtil;
import com.yan.xiaoyanqtserve.entity.KeyInfo;
import com.yan.xiaoyanqtserve.entity.ProductType;
import com.yan.xiaoyanqtserve.security.AdminUserDetails;
import com.yan.xiaoyanqtserve.service.KeyInfoService;
import com.yan.xiaoyanqtserve.service.ProductTypeService;
import com.yan.xiaoyanqtserve.service.impl.KeyInfoManagerService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 密钥信息表 前端控制器
 *
 * @author 烟尘
 * @since 2026-02-03
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/api/admin/keyInfo")
@RequiredArgsConstructor
public class KeyInfoController {
    private final SecurityContextUtil securityContextUtil;
    private final KeyInfoService keyInfoService;
    private final KeyInfoManagerService keyInfoManagerService;
    private final ProductTypeService productTypeService;

    /**
     * 密钥生成
     * @param generateKeyDto 密钥生成参数
     * @return 密钥生成结果
     */
    @PostMapping("/generate")
    public ApiResponse<?> generate(@Valid @RequestBody GenerateKeyDto generateKeyDto) {
        log.info("密钥生成参数：{}", generateKeyDto);
        if (securityContextUtil.isAdmin()) {
            AdminUserDetails adminUser = securityContextUtil.getAdminUser();
            if (adminUser == null) {
                return ApiResponse.fail("无权限");
            }
            List<String> keyList = keyInfoManagerService.generateKey(generateKeyDto, adminUser.getId());
            return ApiResponse.success(keyList);
        }
        return ApiResponse.fail("无权限");
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

    /**
     * 获取随机免费卡密
     * @return 卡密信息
     */
    @GetMapping("/random/free")
    public ApiResponse<KeyInfo> getRandomFreeKey() {
        log.info("获取随机免费卡密");
        if (securityContextUtil.isAdmin()) {
            KeyInfo keyInfo = keyInfoService.getRandomFreeKey();
            if (keyInfo != null) {
                return ApiResponse.success(keyInfo);
            }
            return ApiResponse.fail("暂无可用免费卡密");
        }
        return ApiResponse.fail("无权限");
    }

    /**
     * 获取随机售卖卡密（用于闲鱼售卖）
     * @return 卡密信息
     */
    @GetMapping("/random/sale")
    public ApiResponse<KeyInfo> getRandomSaleKey(@RequestParam Long productTypeId) {
        log.info("获取随机售卖卡密");
        if (securityContextUtil.isAdmin()) {
            ProductType detailById = productTypeService.getDetailById(productTypeId);
            if (detailById == null) {
                return ApiResponse.fail("产品类型不存在");
            }
            if (detailById.getStatus() != 1) {
                return ApiResponse.fail("产品类型已下架");
            }

            KeyInfo keyInfo = keyInfoService.getRandomSaleKey(detailById);
            if (keyInfo != null) {
                return ApiResponse.success(keyInfo);
            }
            return ApiResponse.fail("暂无可用售卖卡密");
        }
        return ApiResponse.fail("无权限");
    }
}
