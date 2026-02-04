package com.yan.xiaoyanqtserve.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yan.xiaoyanqtserve.entity.KeyInfo;
import com.yan.xiaoyanqtserve.service.KeyInfoService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 请求处理服务
 * @author yanchen
 * @since 2026/2/3 18:18
 */
@Service
public class RequestProcessingService {

    @Resource
    private KeyInfoService keyInfoService;

    /**
     * 验证 API Key
     * @param apiKey API Key
     * @return 校验通过返回KeyInfo，否则返回Optional.empty()
     */
    public Optional<KeyInfo> validateKey(String apiKey) {
        // 根据 API Key 查询数据库中的 KeyInfo 记录
        LambdaQueryWrapper<KeyInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(KeyInfo::getKeyValue, apiKey);
        KeyInfo keyInfo = keyInfoService.getOne(queryWrapper);

        // 验证密钥是否存在
        if (keyInfo == null) {
            return Optional.empty();
        }

        // 验证密钥状态是否为启用（status = 1）
        if (keyInfo.getStatus() != 1) {
            return Optional.empty();
        }

        // 验证密钥是否已过期
        LocalDateTime expiryTime = keyInfo.getExpiryTime();
        if (expiryTime != null && expiryTime.isBefore(LocalDateTime.now())) {
            return Optional.empty();
        }

        // 验证密钥剩余使用次数是否足够
        Integer remainingUses = keyInfo.getRemainingUses();
        Integer totalUses = keyInfo.getTotalUses();
        if (totalUses != 0 && (remainingUses == null || remainingUses <= 0)) {
            return Optional.empty();
        }

        return Optional.of(keyInfo);
    }
}
