package com.yan.xiaoyanqtserve.service.impl;

import com.yan.xiaoyanqtserve.common.dto.key.GenerateKeyDto;
import com.yan.xiaoyanqtserve.common.enums.ProductType;
import com.yan.xiaoyanqtserve.common.utils.LicenseKeyGenerator;
import com.yan.xiaoyanqtserve.entity.KeyInfo;
import com.yan.xiaoyanqtserve.service.KeyInfoService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 请求处理服务
 * @author yanchen
 * @since 2026/2/3 18:18
 */
@Service
public class KeyInfoManagerService {

    @Resource
    private LicenseKeyGenerator licenseKeyGenerator;

    @Resource
    private KeyInfoService keyInfoService;

    /**
     * 验证 API Key是否还有效
     * @param apiKey API Key
     * @return 校验通过返回KeyInfo，否则返回Optional.empty()
     */
    public Optional<KeyInfo> validateKey(String apiKey) {
        // 根据 API Key 查询数据库中的 KeyInfo 记录
        KeyInfo keyInfo = keyInfoService.getByKeyValue(apiKey);

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

        // 验证密钥是否还有激活次数（activeNum>0时，reNum=0表示已用完，reNum>0表示剩余激活次数；activeNum=0时，reNum表示无限制）
        Integer activationsNum = keyInfo.getActivationsNum();
        Integer remainingActiveNum = keyInfo.getRemainingActiveNum();
        if (activationsNum > 0 && remainingActiveNum == 0) {
                return Optional.empty();
        }

        // 验证密钥剩余使用次数是否足够（total_uses>0时，re_uses=0表示已用完，re_uses>0表示剩余使用次数；total_uses=0时，re_uses表示无限制）
        Integer totalUses = keyInfo.getTotalUses();
        Integer remainingUses = keyInfo.getRemainingUses();
        if (totalUses > 0 && remainingUses == 0) {
            return Optional.empty();
        }

        return Optional.of(keyInfo);
    }

    /**
     * 激活密钥
     * @param apiKey API Key
     * @return 激活成功返回KeyInfo，否则返回Optional.empty()
     */
    public Optional<KeyInfo> activateKey(String apiKey) {
        // 根据 API Key 查询密钥
        KeyInfo keyInfo = keyInfoService.getByKeyValue(apiKey);

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
        boolean updated = keyInfoService.updateById(keyInfo);
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
    public boolean decrementKeyUses(String apiKey) {
        // 根据 API Key 查询密钥
        KeyInfo keyInfo = keyInfoService.getByKeyValue(apiKey);

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
        return keyInfoService.updateById(keyInfo);
    }


    /**
     * 密钥生成（一小时无限制使用的密钥）
     * @param generateKeyDto 密钥生成参数
     */
    @Transactional(rollbackFor = Exception.class)
    public void generateKey(GenerateKeyDto generateKeyDto) {

        List<String> keyList =
                licenseKeyGenerator.generateKey(generateKeyDto.getKeyNumber());

        List<KeyInfo> entityList = keyList.stream()
                .map(this::buildKeyInfo)
                .toList();

        keyInfoService.saveBatch(entityList, 500);
    }

    private KeyInfo buildKeyInfo(String key) {

        KeyInfo keyInfo = new KeyInfo();

        keyInfo.setKeyValue(key);
        keyInfo.setStatus((byte) 1);
        keyInfo.setTotalUses(0);
        keyInfo.setRemainingUses(0);
        keyInfo.setProductType(ProductType.AI_REWRITE_BASIC.getType());
        keyInfo.setActivationsNum(1);
        keyInfo.setRemainingActiveNum(1);
        keyInfo.setRemark("一小时无限制使用的密钥");
        keyInfo.setCreatedBy(1L);

        return keyInfo;
    }
}
