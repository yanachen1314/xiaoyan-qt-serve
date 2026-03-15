package com.yan.xiaoyanqtserve.service.impl.sys;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yan.xiaoyanqtserve.common.dto.key.KeyInfoPageRequest;
import com.yan.xiaoyanqtserve.entity.KeyInfo;
import com.yan.xiaoyanqtserve.mapper.KeyInfoMapper;
import com.yan.xiaoyanqtserve.service.KeyInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Nullable;
import jakarta.annotation.Resource;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
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

    @Resource
    private KeyInfoMapper keyInfoMapper;

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
     * @param pageRequest 分页查询参数
     * @return 分页结果
     */
    @Override
    public Page<KeyInfo> getList(KeyInfoPageRequest pageRequest) {
        Page<KeyInfo> keyInfoPage = new Page<>(pageRequest.getCurrentPage(), pageRequest.getPageSize());
        
        LambdaQueryWrapper<KeyInfo> queryWrapper = new LambdaQueryWrapper<>();
        
        // 添加查询条件
        if (StringUtils.hasText(pageRequest.getKeyValue())) {
            queryWrapper.like(KeyInfo::getKeyValue, pageRequest.getKeyValue());
        }
        if (pageRequest.getStatus() != null) {
            queryWrapper.eq(KeyInfo::getStatus, pageRequest.getStatus());
        }
        if (pageRequest.getActivated() != null) {
            queryWrapper.eq(KeyInfo::getActivated, pageRequest.getActivated());
        }
        if (StringUtils.hasText(pageRequest.getProductType())) {
            queryWrapper.eq(KeyInfo::getProductType, pageRequest.getProductType());
        }
        
        // 按创建时间倒序
        queryWrapper.orderByDesc(KeyInfo::getCreatedTime);
        
        return this.page(keyInfoPage, queryWrapper);
    }

    /**
     * 更新密钥信息
     * @param keyInfo 密钥信息
     */
    @Override
    public void updateKeyInfo(KeyInfo keyInfo) {
        int i = keyInfoMapper.updateById(keyInfo);
        if (i <= 0) {
            throw new RuntimeException("更新密钥信息失败");
        }
    }
}
