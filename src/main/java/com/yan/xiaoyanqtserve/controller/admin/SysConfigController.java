package com.yan.xiaoyanqtserve.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yan.xiaoyanqtserve.common.dto.adminUser.ConfigPageRequest;
import com.yan.xiaoyanqtserve.common.dto.adminUser.SysConfigDto;
import com.yan.xiaoyanqtserve.common.utils.ApiResponse;
import com.yan.xiaoyanqtserve.entity.SysConfig;
import com.yan.xiaoyanqtserve.service.SysConfigService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 系统全局参数配置表
 *
 * @author 烟尘
 * @since 2026-03-10
 */
@RestController
@RequestMapping("/api/admin/sysConfig")
@RequiredArgsConstructor
public class SysConfigController {

    private final SysConfigService sysConfigService;

    /**
     * 分页查询配置列表
     *
     * @param configPageRequest 分页查询参数
     * @return 分页结果
     */
    @PostMapping("/list")
    public ApiResponse<IPage<SysConfig>> list(@RequestBody ConfigPageRequest configPageRequest) {
        return ApiResponse.success(sysConfigService.listByPage(configPageRequest));
    }

    /**
     * 新增配置
     *
     * @param configDto 配置信息
     * @return 配置结果
     */
    @PostMapping("/add")
    public ApiResponse<String> add(@RequestBody @Valid SysConfigDto configDto) {
        sysConfigService.saveConfig(configDto);
        return ApiResponse.success("新增成功",null);
    }

    /**
     * 更新配置
     *
     * @param config 配置信息
     * @return 更新结果
     */
    @PostMapping("/update")
    public ApiResponse<String> update(@RequestBody @Valid SysConfig config) {
        sysConfigService.update(config);
        return ApiResponse.success("更新成功",null);
    }

    /**
     * 删除配置
     *
     * @param configId 配置 ID
     * @return 删除结果
     */
    @PostMapping("/delete/{configId}")
    public ApiResponse<String> delete(@PathVariable Long configId) {
        sysConfigService.delete(configId);
        return ApiResponse.success("删除成功",null);
    }

    /**
     * 获取可用模型列表
     *
     * @return 模型列表
     */
    @GetMapping("/modelList")
    public ApiResponse<List<String>> getModelList() {
        return ApiResponse.success(sysConfigService.getModelList());
    }

    /**
     * 获取所有配置分组列表
     *
     * @return 分组列表
     */
    @GetMapping("/groupList")
    public ApiResponse<List<String>> getGroupList() {
        return ApiResponse.success(sysConfigService.getGroupList());
    }
}
