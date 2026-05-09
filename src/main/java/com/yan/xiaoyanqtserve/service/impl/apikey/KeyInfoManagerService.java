package com.yan.xiaoyanqtserve.service.impl.apikey;

import com.yan.xiaoyanqtserve.common.Constants;
import com.yan.xiaoyanqtserve.common.dto.key.GenerateKeyDto;
import com.yan.xiaoyanqtserve.common.dto.key.KeyActiveDto;
import com.yan.xiaoyanqtserve.common.dto.key.ValidationResult;
import com.yan.xiaoyanqtserve.common.enums.BizStatus;
import com.yan.xiaoyanqtserve.common.utils.LicenseKeyGenerator;
import com.yan.xiaoyanqtserve.common.utils.RedisLockUtil;
import com.yan.xiaoyanqtserve.common.utils.RedisUtil;
import com.yan.xiaoyanqtserve.entity.KeyInfo;
import com.yan.xiaoyanqtserve.common.exception.BizException;
import com.yan.xiaoyanqtserve.entity.ProductType;
import com.yan.xiaoyanqtserve.service.KeyInfoService;
import com.yan.xiaoyanqtserve.service.ProductTypeService;
import com.yan.xiaoyanqtserve.task.MailRecordSyncTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * APIKEY请求处理服务
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
    private final RedisUtil redisUtil;

    private final MailRecordSyncTask mailRecordSyncTask;

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
            log.info("此密钥的数据非法: key={}, totalUses={}, remainingUses={}",
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
                log.info("此密钥的数据非法: key={}, totalUses=0, remainingUses={}",
                        keyInfo.getKeyValue(), remainingUses);
                return ValidationResult.failure(BizStatus.KEY_INVALID_STATE);
            }
            // remainingUses == 0：合法，密钥无使用次数限制
        } else {
            // 5.3 其他情况：非法状态
            log.info("此密钥的数据非法: key={}, totalUses={}, remainingUses={}",
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
     *
     * @param keyActiveDto 激活参数（deviceID）
     * @param apiKey 激活的密钥
     * @return 激活成功的密钥信息
     */
    @Transactional(rollbackFor = Exception.class)
    public KeyInfo activateKey(KeyActiveDto keyActiveDto, String apiKey) {
        String lockKey = Constants.REDIS_LOCK_PREFIX + "activateKey:" + apiKey;
        if (!redisLockUtil.lock(lockKey, 10)) {
            throw new BizException("系统繁忙，请稍后再试！");
        }

        try {
            KeyInfo keyInfo = keyInfoService.getByKeyValue(apiKey);
            // 设备ID
            String deviceID = keyActiveDto.getDeviceID();

            // 1. 校验密钥
            ValidationResult keyValid = activeKeyValid(keyInfo, deviceID);
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
                // 2.1 扣减剩余激活次数
                // 若 actNum == 0（无限次），remaining_active_num 保持原值（应为 0），无需更新
                // 如果 actNum > 0（有限次），则需要在新设备激活时扣减 remaining_active_num 一次
                if (actNum > 0 && isNewDevice(validKeyInfo.getActivationDevice(), deviceID)) {
                    validKeyInfo.setRemainingActiveNum(remAct - 1);
                }

                // 2.2 更新激活时间
                validKeyInfo.setActivationTime(LocalDateTime.now());
                // 2.3 更新设备
                validKeyInfo.setActivationDevice(getNewKeyDevice(validKeyInfo.getActivationDevice(), deviceID));

                // 2.4 更新备注

                // 2.5 执行更新
                keyInfoService.updateKeyInfo(validKeyInfo);
                // 2.6 返回最新结果
                return validKeyInfo;
            }

            // 3. 首次激活
            // 3.1 设置激活状态和销售状态
            validKeyInfo.setActivated((byte) 1);
            validKeyInfo.setSelled((byte) 1);


            // 3.2 扣减激活次数（仅限有限次数场景）
            // 若 actNum == 0（无限次），remaining_active_num 保持原值（应为 0），无需更新
            if (actNum > 0) {
                validKeyInfo.setRemainingActiveNum(remAct - 1);
            }

            // 3.4 设置激活时间
            LocalDateTime now = LocalDateTime.now();
            validKeyInfo.setActivationTime(now);
            // 3.3 设置激活设备
            validKeyInfo.setActivationDevice(getNewKeyDevice(validKeyInfo.getActivationDevice(), deviceID));
            // 3.5 设置过期时间
            ProductType productType = productTypeService.getDetailById(validKeyInfo.getProductType().longValue());
            if (productType == null) {
                log.info("产品类型不存在: {}", validKeyInfo.getProductType());
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

        // 1. 状态必须启用
        if (keyInfo.getStatus() == 0) {
            return ValidationResult.failure("密钥已被禁用", BizStatus.KEY_INACTIVE);
        }

        // 2. 新设备需要检查激活次数是否充足
        if (isNewDevice(keyInfo.getActivationDevice(), deviceID)) {
            // 总激活次数
            Integer actNum = keyInfo.getActivationsNum();
            // 剩余激活次数
            Integer remAct = keyInfo.getRemainingActiveNum();

            if (actNum != null && actNum > 0) {
                // 有限激活次数： remaining_active_num 必须不为 null 且 > 0
                if (remAct == null || remAct <= 0) {
                    return ValidationResult.failure("激活设备数已达上限", BizStatus.KEY_NON_ACTIVATIONS);
                }
            } else if (actNum != null && actNum == 0) {
                // 无限激活次数：remaining_active_num 必须为 0（按约定）
                if (remAct != null && remAct != 0) {
                    log.info("密钥数据异常: key={}, actNum=0, remAct={}", keyInfo.getKeyValue(), remAct);
                    return ValidationResult.failure("密钥数据异常，请联系管理员", BizStatus.KEY_INVALID_STATE);
                }
            } else {
                log.info("激活次数配置异常: key={}, actNum={}, remAct={}", keyInfo.getKeyValue(), actNum, remAct);
                return ValidationResult.failure("激活次数配置异常", BizStatus.KEY_INVALID_STATE);
            }
        }


        // 3. 检查过期时间（已激活才进行检查）
        Byte activated = keyInfo.getActivated();
        if (activated != null && activated == 1) {
            LocalDateTime expiryTime = keyInfo.getExpiryTime();
            if (expiryTime != null && expiryTime.isBefore(LocalDateTime.now())) {
                return ValidationResult.failure("密钥已过期", BizStatus.KEY_EXPIRED);
            }
        }

        return ValidationResult.success(keyInfo);
    }

    /**
     * 获取新的密钥设备字符串，将设备ID添加到现有设备列表中，设备ID之间用逗号分隔
     * @param activationDevice 现有设备列表，用逗号分隔
     * @param deviceID 设备ID
     * @return 新的密钥设备字符串
     */
    private String getNewKeyDevice(String activationDevice, String deviceID) {
        if (activationDevice == null) {
            return deviceID;
        }

        List<String> deviceList = parseKeyDevice(activationDevice);
        if (deviceList.contains(deviceID)) {
            return activationDevice;
        }
        return activationDevice + "," + deviceID;
    }

    /**
     * 判断是否为新设备
     * @param activationDevice 现有设备列表，用逗号分隔
     * @param deviceID 设备ID
     * @return 是否为新设备，true 表示是新设备，false 表示不是新设备
     */
    private boolean isNewDevice(String activationDevice, String deviceID) {
        return !parseKeyDevice(activationDevice).contains(deviceID);
    }

    /**
     * 解析密钥设备字符串，返回设备ID列表
     * @param activationDevice 密钥设备字符串，用逗号分隔
     * @return 设备ID列表
     */
    private List<String> parseKeyDevice(String activationDevice) {
        if (activationDevice == null) return Collections.emptyList();
        return Arrays.asList(activationDevice.split(","));
    }


    /**
     * 密钥生成
     * @param operator 操作员
     * @param generateKeyDto 密钥生成参数
     */
    @Transactional(rollbackFor = Exception.class)
    public List<String> generateKey(GenerateKeyDto generateKeyDto, Long operator) {
        // 生成若干密钥
        List<String> keyList = licenseKeyGenerator.generateKey(generateKeyDto.getKeyNumber());

        // 查询产品类型
        ProductType productType = productTypeService.getDetailById(generateKeyDto.getProductTypeId().longValue());
        if (productType == null) {
            log.info("产品类型不存在: {}", generateKeyDto.getProductTypeId());
            throw new BizException("激活失败");
        }

        // 批量保存密钥信息
        List<KeyInfo> entityList = keyList.stream()
                .map(key -> buildKeyInfo(key, operator, generateKeyDto, productType))
                .toList();
        keyInfoService.saveBatch(entityList, 500);

        return keyList;
    }

    /**
     * 构建密钥信息
     * <br>
     * 生产密钥对象的辅助函数
     * @param key 密钥
     * @param operator 操作员
     * @param generateKeyDto 密钥生成参数
     * @param productType 产品类型
     * @return 密钥信息
     */
    public KeyInfo buildKeyInfo(String key, Long operator, GenerateKeyDto generateKeyDto, ProductType productType) {
        KeyInfo keyInfo = new KeyInfo();

        keyInfo.setKeyValue(key);
        // 设置默认启用状态
        keyInfo.setStatus((byte) 1);
        // 设置默认未售出
        keyInfo.setSelled((byte) 0);
        // 设置使用类型
        keyInfo.setUsageType(generateKeyDto.getUsageType());

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
     * @param apiKeyUser API Key用户信息
     */
    @Transactional(rollbackFor = Exception.class)
    public void decrementKeyUses(KeyInfo apiKeyUser) {
        String lockKey = Constants.REDIS_LOCK_PREFIX + "decrementKeyUses:" + apiKeyUser.getKeyValue();
        if (!redisLockUtil.lock(lockKey, 10)) {
            throw new BizException("系统繁忙，请稍后再试！");
        }
        try {
            Integer totalUses = apiKeyUser.getTotalUses();
            Integer remainingUses = apiKeyUser.getRemainingUses();
            if (totalUses != null && totalUses > 0 && remainingUses != null && remainingUses > 0) {
                apiKeyUser.setRemainingUses(remainingUses - 1);
                // 保存更新后的密钥信息
                keyInfoService.updateKeyInfo(apiKeyUser);
            }
        } finally {
            redisLockUtil.unlock(lockKey);
        }
    }


    /**
     * 从密钥池中获取一个未使用的免费密钥
     * @return 未使用的免费密钥
     */
    public String getUnusedFreeKeyFromPool() {
        // 从redis中的免费密钥池获取一个未使用的密钥值
        Object freeKeyValueObj = redisUtil.sPop(Constants.FREE_KEY_POOL);
        // 如果为空，尝试一次补充免费密钥池
        if (freeKeyValueObj == null) {
            log.warn("免费密钥池已空，开始自动补充");
            try {
                mailRecordSyncTask.replenishFreeKeyPool();
            } catch (Exception e) {
                throw new BizException("补充免费密钥池失败，请联系客服！");
            }

            // 再次尝试从Redis池中获取
            freeKeyValueObj = redisUtil.sPop(Constants.FREE_KEY_POOL);

            // 再次检查是否为空
            if (freeKeyValueObj == null) {
                throw new BizException("免费密钥池补充后仍无可用密钥，请联系客服！");
            }
        }

        // 从密钥池中获取一个未使用的免费密钥
        String freeKeyValue = freeKeyValueObj.toString();

        // 增加今日发放计数
        String today = LocalDate.now().toString();
        String countKey = Constants.FREE_KEY_DAILY_COUNT + today;
        redisUtil.incr(countKey, 1);

        return freeKeyValue;
    }


    /**
     * 将卡密归还到密钥池，并递减今日发放计数
     * <p>
     * 用于在保存密钥记录事务回滚时，将已弹出的卡密恢复到池中
     * </p>
     * @param freeKeyValue 要归还的卡密值
     */
    public void returnFreeKeyToPool(String freeKeyValue) {
        if(!StringUtils.hasText(freeKeyValue)) {
            return;
        }
        try {
            // 将卡密重新添加到池中
            redisUtil.sAdd(Constants.FREE_KEY_POOL, freeKeyValue);
            // 递减今日发放计数
            String today = LocalDate.now().toString();
            String countKey = Constants.FREE_KEY_DAILY_COUNT + today;
            redisUtil.decr(countKey, 1);
            log.info("卡密已归还到密钥池: {}", freeKeyValue);
        } catch (Exception returnEx) {
            log.error("卡密归还池中失败，卡密: {}, 原因: {}", freeKeyValue , returnEx.getMessage(), returnEx);
        }
    }
}
