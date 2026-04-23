package com.yan.xiaoyanqtserve.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yan.xiaoyanqtserve.common.dto.product.ProductTypeDto;
import com.yan.xiaoyanqtserve.common.dto.product.ProductTypePageRequest;
import com.yan.xiaoyanqtserve.entity.ProductType;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 产品信息表 服务类
 * </p>
 *
 * @author 烟尘
 * @since 2026-03-14
 */
public interface ProductTypeService extends IService<ProductType> {

    /**
     * 分页查询产品类型列表
     * @param pageRequest 分页查询参数
     * @return 分页结果
     */
    Page<ProductType> getList(ProductTypePageRequest pageRequest);

    /**
     * 根据id获取产品类型详情
     * @param id 产品类型id
     * @return 产品类型详情
     */
    ProductType getDetailById(Long id);


    /**
     * 新增产品类型
     * @param dto 产品类型信息
     */
    void add(ProductTypeDto dto);

    /**
     * 修改产品类型
     * @param dto 产品类型信息
     */
    void update(ProductTypeDto dto);

    /**
     * 根据id删除产品类型
     * @param id 产品类型id
     */
    void deleteById(Long id);

    /**
     * 根据产品编码获取产品类型
     * @param freeKeyProductCode 产品编码
     * @return 产品详情
     */
    ProductType getByProductCode(String freeKeyProductCode);
}
