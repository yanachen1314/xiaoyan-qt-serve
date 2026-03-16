package com.yan.xiaoyanqtserve.service.impl.sys;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.enums.SqlKeyword;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yan.xiaoyanqtserve.common.Constants;
import com.yan.xiaoyanqtserve.common.dto.key.KeyInfoPageRequest;
import com.yan.xiaoyanqtserve.common.utils.RedisLockUtil;
import com.yan.xiaoyanqtserve.entity.KeyInfo;
import com.yan.xiaoyanqtserve.entity.ProductType;
import com.yan.xiaoyanqtserve.mapper.KeyInfoMapper;
import com.yan.xiaoyanqtserve.service.KeyInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Nullable;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

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
    @Resource
    private RedisLockUtil redisLockUtil;

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

        if (StringUtils.hasText(pageRequest.getUsageType())) {
            queryWrapper.eq(KeyInfo::getUsageType, pageRequest.getUsageType());
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

    /**
     * 获取随机免费卡密
     * @return 卡密信息，如果没有则返回 null
     */
    @Nullable
    @Override
    @Transactional(rollbackFor = Exception.class)
    public KeyInfo getRandomFreeKey() {
        String lockKey = Constants.REDIS_LOCK_PREFIX + "random_free_key";
        if (!redisLockUtil.lock(lockKey)) {
            throw new RuntimeException("系统繁忙，请稍后再试！");
        }
        try {
            // 构建查询条件，使用 apply 添加数据库特定的随机排序函数，
            // 并使用 last("LIMIT 1") 限制只返回一条记录，提高查询效率
            LambdaQueryWrapper<KeyInfo> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(KeyInfo::getActivated, 0)  // 未激活
                    .eq(KeyInfo::getStatus, 1)          // 已启用
                    .eq(KeyInfo::getSelled, 0)          // 未售卖
                    .eq(KeyInfo::getProductType, 2)     // 免费卡密（ID为2，10分钟体验卡）
                    .eq(KeyInfo::getUsageType, "apikey.usageType.free")
                    .last("ORDER BY RAND() LIMIT 1");  // 使用 RAND() 进行随机排序，只取排序后的一个结果

            List<KeyInfo> list = list(queryWrapper);

            // 如果查询结果为空，则返回 null；否则返回第一个（也是唯一一个）元素
            if (list.isEmpty()) return null;
            KeyInfo keyInfo = list.get(0);
            // 创建一个副本，避免修改原始对象
            KeyInfo copyed_info = new KeyInfo();
            BeanUtils.copyProperties(keyInfo, copyed_info);
            copyed_info.setSelled((byte) 1);
            // 更新数据库中的记录
            updateKeyInfo(copyed_info);
            // 返回原始对象，避免修改副本
            return keyInfo;
        } finally {
            redisLockUtil.unlock(lockKey);
        }
    }

    /**
     * 获取随机售卖卡密（用于闲鱼售卖）
     * @return 卡密信息，如果没有则返回 null
     */
    @Nullable
    @Override
    public KeyInfo getRandomSaleKey(ProductType productType) {
        String lockKey = Constants.REDIS_LOCK_PREFIX + "random_sale_key";
        if (!redisLockUtil.lock(lockKey)) {
            throw new RuntimeException("系统繁忙，请稍后再试！");
        }
        try {
            // 构建查询条件，使用 apply 添加数据库特定的随机排序函数，
            // 并使用 last("LIMIT 1") 限制只返回一条记录，提高查询效率
            LambdaQueryWrapper<KeyInfo> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(KeyInfo::getActivated, 0)  // 未激活
                    .eq(KeyInfo::getStatus, 1)          // 已启用
                    .eq(KeyInfo::getSelled, 0)          // 未售卖
                    .eq(KeyInfo::getProductType, productType.getId())
                    .eq(KeyInfo::getUsageType, "apikey.usageType.xianyu")
                    .last("ORDER BY RAND() LIMIT 1");  // 使用 RAND() 进行随机排序，只取排序后的一个结果

            List<KeyInfo> list = list(queryWrapper);

            // 如果查询结果为空，则返回 null；否则返回第一个（也是唯一一个）元素
            if (list.isEmpty()) return null;
            KeyInfo keyInfo = list.get(0);
            // 创建一个副本，避免修改原始对象
            KeyInfo copyed_info = new KeyInfo();
            BeanUtils.copyProperties(keyInfo, copyed_info);
            copyed_info.setSelled((byte) 1);
            // 更新数据库中的记录
            updateKeyInfo(copyed_info);
            // 返回原始对象，避免修改副本
            return keyInfo;
        } finally {
            redisLockUtil.unlock(lockKey);
        }
    }
}
