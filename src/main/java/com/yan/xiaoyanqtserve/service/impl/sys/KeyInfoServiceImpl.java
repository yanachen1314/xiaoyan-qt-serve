package com.yan.xiaoyanqtserve.service.impl.sys;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yan.xiaoyanqtserve.entity.KeyInfo;
import com.yan.xiaoyanqtserve.mapper.KeyInfoMapper;
import com.yan.xiaoyanqtserve.service.KeyInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

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
     * 激活密钥
     * @param apiKey API Key
     * @return 激活成功返回KeyInfo，否则返回Optional.empty()
     */
    @Override
    public Optional<KeyInfo> activateKey(String apiKey) {
        // 根据 API Key 查询密钥
        LambdaQueryWrapper<KeyInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(KeyInfo::getKeyValue, apiKey);
        KeyInfo keyInfo = getOne(queryWrapper);

        // 验证密钥是否存在
        if (keyInfo == null) {
            return Optional.empty();
        }

        // 验证密钥状态是否为启用
        if (keyInfo.getStatus() != 1) {
            return Optional.empty();
        }

        // 更新密钥的激活时间
        keyInfo.setActivationTime(LocalDateTime.now());

        // 保存更新后的密钥信息
        boolean updated = updateById(keyInfo);
        if (!updated) {
            return Optional.empty();
        }

        return Optional.of(keyInfo);
    }

    /**
     * 扣减密钥使用次数
     * @param apiKey API Key
     * @return 扣减成功返回true，否则返回false
     */
    @Override
    public boolean decrementKeyUses(String apiKey) {
        // 根据 API Key 查询密钥
        LambdaQueryWrapper<KeyInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(KeyInfo::getKeyValue, apiKey);
        KeyInfo keyInfo = getOne(queryWrapper);

        // 验证密钥是否存在
        if (keyInfo == null) {
            return false;
        }

        // 验证密钥状态是否为启用
        if (keyInfo.getStatus() != 1) {
            return false;
        }

        // 处理无限制使用次数的情况（total_uses = 0）
        if (keyInfo.getTotalUses() == 0) {
            return true;
        }

        // 扣减剩余使用次数
        Integer remainingUses = keyInfo.getRemainingUses();
        if (remainingUses == null || remainingUses <= 0) {
            return false;
        }

        keyInfo.setRemainingUses(remainingUses - 1);

        // 保存更新后的密钥信息
        return updateById(keyInfo);
    }
}

