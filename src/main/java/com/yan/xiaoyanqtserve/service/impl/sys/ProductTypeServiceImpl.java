package com.yan.xiaoyanqtserve.service.impl.sys;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yan.xiaoyanqtserve.common.dto.product.ProductTypeDto;
import com.yan.xiaoyanqtserve.common.dto.product.ProductTypePageRequest;
import com.yan.xiaoyanqtserve.common.utils.RedisUtil;
import com.yan.xiaoyanqtserve.entity.ProductType;
import com.yan.xiaoyanqtserve.mapper.ProductTypeMapper;
import com.yan.xiaoyanqtserve.service.ProductTypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

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
    @Resource
    private RedisUtil redisUtil;

    private final static String CACHE_KEY = "productTypeCache:";

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

    /**
     * 根据id获取产品类型详情
     * @param id 产品类型id
     * @return 产品类型详情
     */
    @Override
    public ProductType getDetailById(Long id) {
        if (redisUtil.hasKey(CACHE_KEY + id)) {
            return (ProductType) redisUtil.get(CACHE_KEY + id);
        }
        ProductType productType = productTypeMapper.selectById(id);
        redisUtil.set(CACHE_KEY + id, productType,5, TimeUnit.MINUTES);
        return productType;
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
        redisUtil.delete(CACHE_KEY + dto.getId());
    }

    /**
     * 根据id删除产品类型
     * @param id 产品类型id
     */
    @Override
    public void deleteById(Long id) {
        productTypeMapper.deleteById(id);
        redisUtil.delete(CACHE_KEY + id);
    }

    /**
     * 根据产品编码获取产品类型（模糊搜索）
     * @param freeKeyProductCode 产品编码
     * @return 产品详情
     */
    @Override
    public ProductType getByProductCode(String freeKeyProductCode) {
        LambdaQueryWrapper<ProductType> queryWrapper = new LambdaQueryWrapper<>();
        // 添加查询条件
        if (StringUtils.hasText(freeKeyProductCode)) {
            queryWrapper.like(ProductType::getProductCode, freeKeyProductCode);
        }
        return this.getOne(queryWrapper);
    }

}
