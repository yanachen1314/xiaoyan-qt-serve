package com.yan.xiaoyanqtserve.service.impl.sys;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yan.xiaoyanqtserve.common.dto.product.ProductTypeDto;
import com.yan.xiaoyanqtserve.common.dto.product.ProductTypePageRequest;
import com.yan.xiaoyanqtserve.entity.ProductType;
import com.yan.xiaoyanqtserve.mapper.ProductTypeMapper;
import com.yan.xiaoyanqtserve.service.ProductTypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * <p>
 * 产品信息表 服务实现类
 * </p>
 *
 * @author 烟尘
 * @since 2026-03-14
 */
@Service
public class ProductTypeServiceImpl extends ServiceImpl<ProductTypeMapper, ProductType> implements ProductTypeService {

    @Resource
    private ProductTypeMapper productTypeMapper;

    /**
     * 分页查询产品类型列表
     * @param pageRequest 分页查询参数
     * @return 分页结果
     */
    @Override
    public Page<ProductType> getList(ProductTypePageRequest pageRequest) {
        Page<ProductType> page = new Page<>(pageRequest.getCurrentPage(), pageRequest.getPageSize());
        
        LambdaQueryWrapper<ProductType> queryWrapper = new LambdaQueryWrapper<>();
        
        // 添加查询条件
        if (StringUtils.hasText(pageRequest.getTypeName())) {
            queryWrapper.like(ProductType::getProductName, pageRequest.getTypeName());
        }
        if (pageRequest.getStatus() != null) {
            queryWrapper.eq(ProductType::getStatus, pageRequest.getStatus());
        }
        
        // 按排序字段排序
        queryWrapper.orderByAsc(ProductType::getSortOrder);
        
        return this.page(page, queryWrapper);
    }

    @Override
    public ProductType getDetailById(Long id) {
        return productTypeMapper.selectById(id);
    }

    /**
     * 新增产品类型
     * @param dto 产品类型信息
     */
    @Override
    public void add(ProductTypeDto dto) {
        ProductType productType = new ProductType();
        BeanUtils.copyProperties(dto, productType);
        productType.setCreateTime(LocalDateTime.now());
        productType.setUpdateTime(LocalDateTime.now());
        
        this.save(productType);
    }

    /**
     * 修改产品类型
     * @param dto 产品类型信息
     */
    @Override
    public void update(ProductTypeDto dto) {
        ProductType productType = new ProductType();
        BeanUtils.copyProperties(dto, productType);
        productType.setUpdateTime(LocalDateTime.now());
        
        this.updateById(productType);
    }
}
