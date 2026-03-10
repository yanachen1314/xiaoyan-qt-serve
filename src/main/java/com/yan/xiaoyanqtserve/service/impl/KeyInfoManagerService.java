package com.yan.xiaoyanqtserve.service.impl;

import com.yan.xiaoyanqtserve.common.dto.key.GenerateKeyDto;
import com.yan.xiaoyanqtserve.common.dto.key.KeyActiveDto;
import com.yan.xiaoyanqtserve.common.dto.key.ValidationResult;
import com.yan.xiaoyanqtserve.common.enums.ProductType;
import com.yan.xiaoyanqtserve.common.enums.BizStatus;
import com.yan.xiaoyanqtserve.common.utils.LicenseKeyGenerator;
import com.yan.xiaoyanqtserve.entity.KeyInfo;
import com.yan.xiaoyanqtserve.exception.BizException;
import com.yan.xiaoyanqtserve.service.KeyInfoService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

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
     * @return 验证结果
     */
    public ValidationResult validateKey(String apiKey) {
        // 根据 API Key 查询数据库中的 KeyInfo 记录
        KeyInfo keyInfo = keyInfoService.getByKeyValue(apiKey);

        // 验证密钥是否存在
        if (keyInfo == null) {
            return ValidationResult.failure(BizStatus.KEY_NOT_FOUND);
        }

        // 验证密钥格式是否非法
        if (!licenseKeyGenerator.verify(apiKey)) {
            return ValidationResult.failure(BizStatus.KEY_FORMAT_INVALID);
        }

        // 验证密钥状态是否为启用（status = 1）
        if (keyInfo.getStatus() != 1) {
            return ValidationResult.failure(BizStatus.KEY_INACTIVE);
        }

        // 验证密钥是否已过期
        LocalDateTime expiryTime = keyInfo.getExpiryTime();
        if (expiryTime != null && expiryTime.isBefore(LocalDateTime.now())) {
            return ValidationResult.failure(BizStatus.KEY_EXPIRED);
        }

        // 验证密钥是否还有激活次数（activeNum>0时，reNum=0表示已用完，reNum>0表示剩余激活次数；activeNum=0时，reNum表示无限制）
        Integer activationsNum = keyInfo.getActivationsNum();
        Integer remainingActiveNum = keyInfo.getRemainingActiveNum();
        if (activationsNum > 0 && remainingActiveNum == 0) {
            return ValidationResult.failure(BizStatus.KEY_NON_ACTIVATIONS);
        }

        // 验证密钥剩余使用次数是否足够（total_uses>0时，re_uses=0表示已用完，re_uses>0表示剩余使用次数；total_uses=0时，re_uses表示无限制）
        Integer totalUses = keyInfo.getTotalUses();
        Integer remainingUses = keyInfo.getRemainingUses();
        if (totalUses > 0 && remainingUses == 0) {
            return ValidationResult.failure(BizStatus.KEY_NON_USES);
        }

        return ValidationResult.success(keyInfo);
    }

    /**
     * 激活密钥
     *
     * @param keyActiveDto 激活密钥参数
     */
    @Transactional(rollbackFor = Exception.class)
    public void activateKey(KeyActiveDto keyActiveDto) {
        // 根据 API Key 查询密钥
        ValidationResult validationResult = validateKey(keyActiveDto.getApiKey());

        // 校验失败，返回错误信息
        if (!validationResult.isSuccess()) {
            throw new BizException(validationResult.getErrorCode(),
                    validationResult.getErrorMessage() == null ? validationResult.getErrorCode().msg(): validationResult.getErrorMessage());
        }

        KeyInfo keyInfo = validationResult.getKeyInfo();
        // 更新密钥的激活设备
        keyInfo.setActivationDevice(keyActiveDto.getDeviceID());
        // 扣减密钥的激活次数
        keyInfo.setRemainingActiveNum(keyInfo.getRemainingActiveNum() - 1);
        // 更新密钥的激活时间
        LocalDateTime now = LocalDateTime.now();
        keyInfo.setActivationTime(now);
        // 设置密钥的到期时间（一个小时后过期）
        keyInfo.setExpiryTime(now.plusHours(1));
        // 保存更新后的密钥信息
        boolean updated = keyInfoService.updateById(keyInfo);
        if (!updated) {
            throw new BizException("密钥激活异常，请稍后再试！");
        }
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
