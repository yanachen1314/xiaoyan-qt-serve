package com.yan.xiaoyanqtserve.service.impl.sys;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yan.xiaoyanqtserve.entity.KeyInfo;
import com.yan.xiaoyanqtserve.mapper.KeyInfoMapper;
import com.yan.xiaoyanqtserve.service.KeyInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * <p>
 * 密钥信息表 服务实现类
 * </p>
 *
 * @author 烟尘
 * @since 2026-02-03
 */
@Service
public class KeyInfoServiceImpl extends ServiceImpl<KeyInfoMapper, KeyInfo> implements KeyInfoService {

    /**
     * 根据密钥值查询密钥信息
     * @param keyValue 密钥值
     * @return 密钥信息
     */
    @Nullable
    @Override
    public KeyInfo getByKeyValue(String keyValue) {
        LambdaQueryWrapper<KeyInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(KeyInfo::getKeyValue, keyValue);
        return getOne(queryWrapper);
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
    @Override
    public Page<KeyInfo> getList(Integer page, Integer pageSize, Byte status, Byte activated, String productType) {
        Page<KeyInfo> keyInfoPage = new Page<>(page, pageSize);
        
        LambdaQueryWrapper<KeyInfo> queryWrapper = new LambdaQueryWrapper<>();
        
        // 添加查询条件
        if (status != null) {
            queryWrapper.eq(KeyInfo::getStatus, status);
        }
        if (activated != null) {
            queryWrapper.eq(KeyInfo::getActivated, activated);
        }
        if (StringUtils.hasText(productType)) {
            queryWrapper.eq(KeyInfo::getProductType, productType);
        }
        
        // 按创建时间倒序
        queryWrapper.orderByDesc(KeyInfo::getCreatedTime);
        
        return this.page(keyInfoPage, queryWrapper);
    }
}
