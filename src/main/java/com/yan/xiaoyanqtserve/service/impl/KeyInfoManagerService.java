package com.yan.xiaoyanqtserve.service.impl;

import com.yan.xiaoyanqtserve.common.dto.key.GenerateKeyDto;
import com.yan.xiaoyanqtserve.common.dto.key.KeyActiveDto;
import com.yan.xiaoyanqtserve.common.dto.key.ValidationResult;
import com.yan.xiaoyanqtserve.common.enums.BizStatus;
import com.yan.xiaoyanqtserve.common.utils.LicenseKeyGenerator;
import com.yan.xiaoyanqtserve.common.utils.RedisLockUtil;
import com.yan.xiaoyanqtserve.entity.KeyInfo;
import com.yan.xiaoyanqtserve.common.exception.BizException;
import com.yan.xiaoyanqtserve.entity.ProductType;
import com.yan.xiaoyanqtserve.service.KeyInfoService;
import com.yan.xiaoyanqtserve.service.ProductTypeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * 请求处理服务
 * @author yanchen
 * @since 2026/2/3 18:18
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KeyInfoManagerService {

    private final LicenseKeyGenerator licenseKeyGenerator;
    private final KeyInfoService keyInfoService;
    private final ProductTypeService productTypeService;

    private final RedisLockUtil redisLockUtil;


    /**
     * 验证 API Key 是否具备使用的资格。
     * @param apiKey 待验证的 API Key 字符串
     * @return 验证结果：成功时返回 {@link ValidationResult#success(KeyInfo)}，失败时返回带具体错误码的失败结果
     */
    public ValidationResult filterKeyValid(String apiKey) {
        // 1. 查询密钥信息
        KeyInfo keyInfo = keyInfoService.getByKeyValue(apiKey);
        if (keyInfo == null) {
            return ValidationResult.failure(BizStatus.KEY_NOT_FOUND);
        }

        // 2. 校验密钥格式合法性（防伪造/篡改）
        if (!licenseKeyGenerator.verify(apiKey)) {
            return ValidationResult.failure(BizStatus.KEY_INVALID_FORMAT);
        }

        // 3. 校验密钥启用状态：仅 status = 1 允许使用
        if (keyInfo.getStatus() != 1) {
            return ValidationResult.failure(BizStatus.KEY_INACTIVE);
        }

        // 4. 校验激活状态：activated = 1 才表示密钥已完成首次激活
        // 若已激活的密钥，无论剩余激活次数多少，均可继续调用服务（即不再检查 remaining_active_num）
        Byte activated = keyInfo.getActivated();
        if (activated == null || activated == 0) {
            return ValidationResult.failure(BizStatus.KEY_NOT_ACTIVATED);
        }

        // 5. 校验密钥权益：使用次数是否充足
        Integer totalUses = keyInfo.getTotalUses();
        Integer remainingUses = keyInfo.getRemainingUses();

        if (totalUses == null || remainingUses == null) {
            log.warn("此密钥的数据非法: key={}, totalUses={}, remainingUses={}",
                    keyInfo.getKeyValue(), totalUses, remainingUses);
            return ValidationResult.failure(BizStatus.KEY_INVALID_STATE);
        }

        if (totalUses > 0) {
            // 5.1 有限使用次数，如果剩余使用次数 <= 0，则表示已用尽
            if (remainingUses <= 0) {
                return ValidationResult.failure(BizStatus.KEY_NON_USES);
            }
        } else if (totalUses == 0) {
            // 5.2 无限制使用次数，remaining_uses 必须为 0（业务强约束）
            if (remainingUses != 0) {
                log.warn("此密钥的数据非法: key={}, totalUses=0, remainingUses={}",
                        keyInfo.getKeyValue(), remainingUses);
                return ValidationResult.failure(BizStatus.KEY_INVALID_STATE);
            }
            // remainingUses == 0：合法，密钥无使用次数限制
        } else {
            // 5.3 其他情况：非法状态
            log.warn("此密钥的数据非法: key={}, totalUses={}, remainingUses={}",
                    keyInfo.getKeyValue(), totalUses, remainingUses);
            return ValidationResult.failure(BizStatus.KEY_INVALID_STATE);
        }

        // 6. 校验有效期：过期密钥不可用
        LocalDateTime expiryTime = keyInfo.getExpiryTime();
        if (expiryTime != null && expiryTime.isBefore(LocalDateTime.now())) {
            return ValidationResult.failure(BizStatus.KEY_EXPIRED);
        }

        // ✅ 所有校验通过：密钥启用、已激活、权益充足、未过期
        return ValidationResult.success(keyInfo);
    }


    /**
     * 激活密钥。
     * <p>
     * 业务规则：
     * <ul>
     *   <li>激活资格：密钥必须启用（status=1）、未激活（activated=0）、未过期</li>
     *   <li>激活次数处理：
     *     <ul>
     *       <li>{@code activations_num > 0}：从 {@code remaining_active_num} 扣 1，扣至 0 后不能再激活</li>
     *       <li>{@code activations_num = 0}：无限制激活，{@code remaining_active_num} 保持 0</li>
     *     </ul>
     *   </li>
     *   <li>激活后状态：{@code activated = 1}，记录设备、时间，并按 {@code product_type} 设置使用权益</li>
     *   <li>幂等性：同一设备重复激活 → 返回已激活状态；不同设备 → 报错</li>
     * </ul>
     *
     * @param keyActiveDto 激活参数（apiKey + deviceID）
     * @return 激活成功的密钥信息
     */
    @Transactional(rollbackFor = Exception.class)
    public KeyInfo activateKey(KeyActiveDto keyActiveDto) {
        String lockKey = "Lock_" + keyActiveDto.getApiKey();
        if (!redisLockUtil.lock(lockKey, 10)) {
            throw new BizException("系统繁忙，请稍后再试！");
        }

        try {
            KeyInfo keyInfo = keyInfoService.getByKeyValue(keyActiveDto.getApiKey());

            // 1. 校验密钥
            ValidationResult keyValid = activeKeyValid(keyInfo, keyActiveDto.getDeviceID());
            if (!keyValid.isSuccess()) {
                throw new BizException(keyValid.getErrorCode(),
                        keyValid.getErrorMessage() == null ? keyValid.getErrorCode().msg() : keyValid.getErrorMessage());
            }

            KeyInfo validKeyInfo = keyValid.getKeyInfo();
            // 总激活次数
            Integer actNum = validKeyInfo.getActivationsNum();
            // 剩余激活次数
            Integer remAct = validKeyInfo.getRemainingActiveNum();

            // 2. 非首次激活
            if (validKeyInfo.getActivated() == 1) {
                // 2.1 扣减激活次数（仅限有限次数场景）
                // 若 actNum == 0（无限次），remaining_active_num 保持原值（应为 0），无需更新
                if (actNum > 0) {
                    validKeyInfo.setRemainingActiveNum(remAct - 1);
                }
                // 2.2 更新激活时间
                validKeyInfo.setActivationTime(LocalDateTime.now());
                // 2.3 更新设备
                validKeyInfo.setActivationDevice(getNewKeyDevice(validKeyInfo.getActivationDevice(), keyActiveDto.getDeviceID()));

                // 2.4 更新备注

                // 2.5 执行更新
                keyInfoService.updateKeyInfo(validKeyInfo);
                // 2.6 返回最新结果
                return validKeyInfo;
            }

            // 3. 首次激活
            // 3.1 设置激活状态
            validKeyInfo.setActivated((byte) 1);

            // 3.2 扣减激活次数（仅限有限次数场景）
            // 若 actNum == 0（无限次），remaining_active_num 保持原值（应为 0），无需更新
            if (actNum > 0) {
                validKeyInfo.setRemainingActiveNum(remAct - 1);
            }

            // 3.4 设置激活时间
            LocalDateTime now = LocalDateTime.now();
            validKeyInfo.setActivationTime(now);
            // 3.3 设置激活设备
            validKeyInfo.setActivationDevice(getNewKeyDevice(validKeyInfo.getActivationDevice(), keyActiveDto.getDeviceID()));
            // 3.5 设置过期时间
            ProductType productType = productTypeService.getDetailById(validKeyInfo.getProductType().longValue());
            if (productType == null) {
                log.error("产品类型不存在: {}", validKeyInfo.getProductType());
                throw new BizException("激活失败");
            }
            validKeyInfo.setExpiryTime(now.plusMinutes(productType.getEquityTime()));

            // 3.6 更新备注（可选）

            // 3.7 执行更新
            keyInfoService.updateKeyInfo(validKeyInfo);
            // 3.8 返回最新结果
            return validKeyInfo;

        } finally {
            redisLockUtil.unlock(lockKey);
        }
    }

    private ValidationResult activeKeyValid(KeyInfo keyInfo, String deviceID) {
        if (keyInfo == null) {
            return ValidationResult.failure("密钥不存在", BizStatus.KEY_NOT_FOUND);
        }

        // 1 状态必须启用
        if (keyInfo.getStatus() == 0) {
            return ValidationResult.failure("密钥已被禁用", BizStatus.KEY_INACTIVE);
        }

        // 2.1 如果激活过（activated = 1）
        Byte activated = keyInfo.getActivated();
        if (activated != null && activated == 1) {
            // 检查是否同一设备
            if (Objects.equals(keyInfo.getActivationDevice(), deviceID)) {
                return ValidationResult.success(keyInfo); // 已激活且设备一致，直接返回
            } else {
                throw new BizException(BizStatus.KEY_ALREADY_ACTIVATED, "密钥已在其他设备激活");
            }
        }

        // 2.3 检查激活次数是否充足（仅当有限次数时）
        // 总激活次数
        Integer actNum = keyInfo.getActivationsNum();
        // 剩余激活次数
        Integer remAct = keyInfo.getRemainingActiveNum();

        if (actNum != null && actNum > 0) {
            // 有限激活次数： remaining_active_num 必须不为 null 且 > 0
            if (remAct == null || remAct <= 0) {
                return ValidationResult.failure("激活次数已用完", BizStatus.KEY_NON_ACTIVATIONS);
            }
        } else if (actNum != null && actNum == 0) {
            // 无限激活次数：remaining_active_num 必须为 0（按约定）
            if (remAct != null && remAct != 0) {
                log.warn("密钥数据异常: key={}, actNum=0, remAct={}", keyInfo.getKeyValue(), remAct);
                return ValidationResult.failure("密钥数据异常，请联系管理员", BizStatus.KEY_INVALID_STATE);
            }
        } else {
            log.warn("激活次数配置异常: key={}, actNum={}, remAct={}", keyInfo.getKeyValue(), actNum, remAct);
            return ValidationResult.failure("激活次数配置异常", BizStatus.KEY_INVALID_STATE);
        }

        // 2.4 检查过期（未激活的密钥也可能过期）
        LocalDateTime expiryTime = keyInfo.getExpiryTime();
        if (expiryTime != null && expiryTime.isBefore(LocalDateTime.now())) {
            return ValidationResult.failure("密钥已过期", BizStatus.KEY_EXPIRED);
        }

        return ValidationResult.success(keyInfo);
    }

    private String getNewKeyDevice(String activationDevice, String deviceID) {
        return activationDevice + "," + deviceID;
    }

    private List<String> parseKeyDevice(String activationDevice) {
        return Arrays.asList(activationDevice.split(","));
    }


    /**
     * 密钥生成（一小时无限制使用的密钥）
     * @param operator 操作员
     * @param generateKeyDto 密钥生成参数
     */
    @Transactional(rollbackFor = Exception.class)
    public void generateKey(GenerateKeyDto generateKeyDto, Long operator) {
        // 生成若干密钥
        List<String> keyList = licenseKeyGenerator.generateKey(generateKeyDto.getKeyNumber());

        // 查询产品类型
        ProductType productType = productTypeService.getDetailById(generateKeyDto.getProductTypeId().longValue());
        if (productType == null) {
            log.error("产品类型不存在: {}", generateKeyDto.getProductTypeId());
            throw new BizException("激活失败");
        }

        // 批量保存密钥信息
        List<KeyInfo> entityList = keyList.stream()
                .map(key -> buildKeyInfo(key, operator, generateKeyDto, productType))
                .toList();
        keyInfoService.saveBatch(entityList, 500);
    }

    private KeyInfo buildKeyInfo(String key, Long operator, GenerateKeyDto generateKeyDto, ProductType productType) {
        KeyInfo keyInfo = new KeyInfo();

        keyInfo.setKeyValue(key);
        keyInfo.setStatus((byte) 1);

        // 设置默认未激活
        keyInfo.setActivated((byte) 0);
        // 设置套餐类型
        keyInfo.setProductType(generateKeyDto.getProductTypeId());

        // 设置激活次数和使用次数
        if (productType.getEquityUsageNum() == 0) {
            // 使用次数0，代表无限限制
            keyInfo.setTotalUses(0);
            keyInfo.setRemainingUses(0);
        } else {
            keyInfo.setTotalUses(productType.getEquityUsageNum());
            keyInfo.setRemainingUses(productType.getEquityUsageNum());
        }

        if (productType.getEquityActiveNum() == 0) {
            // 激活次数0，代表无限限制
            keyInfo.setActivationsNum(0);
            keyInfo.setRemainingActiveNum(0);
        } else {
            keyInfo.setActivationsNum(productType.getEquityActiveNum());
            keyInfo.setRemainingActiveNum(productType.getEquityActiveNum());
        }

        // 设置备注和创建者
        keyInfo.setRemark(generateKeyDto.getRemark() == null ? "" : generateKeyDto.getRemark());
        keyInfo.setCreatedBy(operator);
        return keyInfo;
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
}
