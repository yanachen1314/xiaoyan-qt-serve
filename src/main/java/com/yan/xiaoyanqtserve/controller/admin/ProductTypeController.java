package com.yan.xiaoyanqtserve.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yan.xiaoyanqtserve.common.dto.product.ProductTypeDto;
import com.yan.xiaoyanqtserve.common.dto.product.ProductTypePageRequest;
import com.yan.xiaoyanqtserve.common.utils.ApiResponse;
import com.yan.xiaoyanqtserve.entity.ProductType;
import com.yan.xiaoyanqtserve.service.ProductTypeService;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 产品信息表
 *
 * @author 烟尘
 * @since 2026-03-14
 */
@Validated
@RestController
@RequestMapping("/api/admin/productType")
public class ProductTypeController {
    @Resource
    private ProductTypeService productTypeService;

    /**
     * 分页查询产品类型列表
     * @param pageRequest 分页查询参数
     * @return 分页结果
     */
    @PostMapping("/list")
    public ApiResponse<Page<ProductType>> list(@RequestBody ProductTypePageRequest pageRequest) {
        Page<ProductType> productTypePage = productTypeService.getList(pageRequest);
        return ApiResponse.success(productTypePage);
    }

    /**
     * 根据 ID 获取产品类型详情
     * @param id 产品类型 ID
     * @return 产品类型信息
     */
    @GetMapping("/{id}")
    public ApiResponse<ProductType> getById(@PathVariable Long id) {
        ProductType productType = productTypeService.getById(id);
        return ApiResponse.success(productType);
    }

    /**
     * 新增产品类型
     * @param dto 产品类型信息
     * @return 操作结果
     */
    @PostMapping
    public ApiResponse<Void> add(@Valid @RequestBody ProductTypeDto dto) {
        productTypeService.add(dto);
        return ApiResponse.success();
    }

    /**
     * 修改产品类型
     * @param dto 产品类型信息
     * @return 操作结果
     */
    @PostMapping("/update")
    public ApiResponse<Void> update(@Valid @RequestBody ProductTypeDto dto) {
        productTypeService.update(dto);
        return ApiResponse.success();
    }

    /**
     * 删除产品类型
     * @param id 产品类型 ID
     * @return 操作结果
     */
    @GetMapping("/delete/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        productTypeService.removeById(id);
        return ApiResponse.success();
    }
}
