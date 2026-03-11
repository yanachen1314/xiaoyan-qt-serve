package com.yan.xiaoyanqtserve.service.impl;

import com.yan.xiaoyanqtserve.common.dto.key.GenerateKeyDto;
import com.yan.xiaoyanqtserve.common.dto.key.KeyActiveDto;
import com.yan.xiaoyanqtserve.common.dto.key.ValidationResult;
import com.yan.xiaoyanqtserve.common.enums.ProductType;
import com.yan.xiaoyanqtserve.common.enums.BizStatus;
import com.yan.xiaoyanqtserve.common.utils.LicenseKeyGenerator;
import com.yan.xiaoyanqtserve.common.utils.RedisLockUtil;
import com.yan.xiaoyanqtserve.entity.KeyInfo;
import com.yan.xiaoyanqtserve.exception.BizException;
import com.yan.xiaoyanqtserve.service.KeyInfoService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

/**
 * 请求处理服务
 * @author yanchen
 * @since 2026/2/3 18:18
 */
@Slf4j
@Service
public class KeyInfoManagerService {

    @Resource
    private LicenseKeyGenerator licenseKeyGenerator;

    @Resource
    private KeyInfoService keyInfoService;

    @Resource
    private RedisLockUtil redisLockUtil;

    /**
     * 验证 API Key 是否具备有效激活/使用的资格。
     * <p>
     * 本方法严格遵循密钥表 {@code key_info} 的字段语义定义：
     * <ul>
     *   <li>{@code status = 1}：密钥启用；否则禁用</li>
     *   <li>{@code activated = 1}：密钥已完成首次激活（即已成功绑定设备或完成激活流程）；
     *       {@code activated = 0} 表示未激活——即使剩余次数 > 0，也禁止任何激活或使用操作。</li>
     *   <li>激活次数规则：
     *     <ul>
     *       <li>{@code activations_num > 0}：有限激活次数；此时 {@code remaining_active_num = 0} 表示已用尽，{@code > 0} 表示剩余可用次数</li>
     *       <li>{@code activations_num = 0}：无限制激活次数；此时根据业务约定，{@code remaining_active_num 必须为 0}（仅作占位，无实际计数意义）li>
     *     </ul>
     *   </li>
     *   <li>使用次数规则（同理）：
     *     <ul>
     *       <li>{@code total_uses > 0}：有限使用次数；{@code remaining_uses = 0} 表示已用尽</li>
     *       <li>{@code total_uses = 0}：无限制使用次数；此时 {@code remaining_uses 必须为 0}</li>
     *     </ul>
     *   </li>
     *   <li>过期时间 {@code expiry_time}：非空且早于当前时间时视为过期</li>
     * </ul>
     * <p>
     * ⚠️ 重要业务规则：
     * <ol>
     *   <li><strong>“已激活”是使用前提</strong>：未激活（{@code activated = 0}）的密钥，无论剩余次数多少，均不可用于激活或调用服务。</li>
     *   <li><strong>无限次场景下剩余字段必须为 0</strong>：若 {@code activations_num = 0} 或 {@code total_uses = 0}，则对应 {@code remaining_*} 字段必须为 0；
     *       若出现非零值，视为数据异常，拒绝请求（防止脏数据导致逻辑错误）。</li>
     *   <li>校验顺序不可颠倒：先状态 → 再激活状态 → 再权益数量 → 最后过期时间。</li>
     * </ol>
     *
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

        // 4. 【核心业务规则】校验激活状态：activated = 1 才表示密钥已完成首次激活
        //    - activated = 0：密钥虽存在且启用，但尚未激活，禁止任何激活/使用行为
        //    - 此检查必须在权益校验前执行，避免误判未激活密钥为“有剩余次数”
        Byte activated = keyInfo.getActivated();
        if (activated == null || activated == 0) {
            return ValidationResult.failure(BizStatus.KEY_NOT_ACTIVATED);
        }

        // 5. 此时已激活，但是过滤器中无需验证是否还有激活次数：已激活的密钥，无论剩余激活次数多少，均可继续调用服务
        // 但必须进行权益数量校验
/*        Integer actNum = keyInfo.getActivationsNum();
        Integer remAct = keyInfo.getRemainingActiveNum();

        // 防御性校验：字段为空视为无效状态
        if (actNum == null || remAct == null) {
            return ValidationResult.failure(BizStatus.KEY_INVALID_STATE);
        }

        if (actNum > 0) {
            // 有限激活次数：剩余次数必须 > 0 才可继续激活
            if (remAct <= 0) {
                return ValidationResult.failure(BizStatus.KEY_NON_ACTIVATIONS);
            }
        } else if (actNum == 0) {
            // 无限制激活次数：根据业务约定，remaining_active_num 必须为 0
            // 若非 0（如被误写为 5），视为数据异常，拒绝请求
            if (remAct != 0) {
                log.warn("Invalid remaining_active_num for unlimited activation key: actNum=0, remAct={}, key={}",
                        remAct, keyInfo.getKeyValue());
                return ValidationResult.failure(BizStatus.KEY_INVALID_STATE);
            }
            // remAct == 0：合法，表示已激活且无限制 → 允许使用
        } else {
            // actNum < 0：非法值，拒绝
            return ValidationResult.failure(BizStatus.KEY_INVALID_STATE);
        }*/

        // 6. 校验使用次数是否充足（同理，仅在已激活前提下）
        Integer totalUses = keyInfo.getTotalUses();
        Integer remainingUses = keyInfo.getRemainingUses();

        if (totalUses == null || remainingUses == null) {
            return ValidationResult.failure(BizStatus.KEY_INVALID_STATE);
        }

        if (totalUses > 0) {
            if (remainingUses <= 0) {
                return ValidationResult.failure(BizStatus.KEY_NON_USES);
            }
        } else if (totalUses == 0) {
            // 无限制使用次数：remaining_uses 必须为 0（业务强约束）
            if (remainingUses != 0) {
                log.warn("Invalid remaining_uses for unlimited use key: totalUses=0, remainingUses={}, key={}",
                        remainingUses, keyInfo.getKeyValue());
                return ValidationResult.failure(BizStatus.KEY_INVALID_STATE);
            }
            // remainingUses == 0：合法
        } else {
            return ValidationResult.failure(BizStatus.KEY_INVALID_STATE);
        }

        // 7. 校验有效期：过期密钥不可用（注意：过期不影响“是否已激活”，故放最后）
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
            // 1. 校验密钥
            ValidationResult keyValid = activeKeyValid(keyActiveDto.getApiKey(), keyActiveDto.getDeviceID());
            if (!keyValid.isSuccess()) {
                throw new BizException(keyValid.getErrorCode(),
                        keyValid.getErrorMessage() == null ? keyValid.getErrorCode().msg() : keyValid.getErrorMessage());
            }

            KeyInfo keyInfo = keyValid.getKeyInfo();
            if(keyInfo.getStatus() == 1
                    && keyInfo.getActivated() == 1
                    && keyInfo.getActivationDevice().equals(keyActiveDto.getDeviceID())){
                // 已激活，直接返回使用
                return keyInfo;
            }

            // 总激活次数
            Integer actNum = keyInfo.getActivationsNum();
            // 剩余激活次数
            Integer remAct = keyInfo.getRemainingActiveNum();

            // 3. 构建更新对象（推荐：只更新需变更字段，避免覆盖 null 值）
            KeyInfo updateEntity = new KeyInfo();
            updateEntity.setId(keyInfo.getId());

            // 3.1 设置激活状态
            updateEntity.setActivated((byte) 1);

            // 3.2 扣减激活次数（仅限有限次数场景）
            if (actNum > 0) {
                updateEntity.setRemainingActiveNum(remAct - 1);
            }
            // 若 actNum == 0（无限次），remaining_active_num 保持原值（应为 0），无需更新

            // 3.3 设置激活信息
            updateEntity.setActivationDevice(keyActiveDto.getDeviceID());
            LocalDateTime now = LocalDateTime.now();
            updateEntity.setActivationTime(now);

            // 3.4 根据 product_type 配置使用权益（关键！）
            configureUsageRights(updateEntity, keyInfo.getProductType(), now);

            // 3.5 更新备注（可选）
            updateEntity.setRemark(String.format("激活时间: %s | 设备: %s",
                    now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                    keyActiveDto.getDeviceID()));

            // 4. 执行更新（MyBatis-Plus updateById 会自动忽略 null 字段）
            boolean updated = keyInfoService.updateById(updateEntity);
            if (!updated) {
                throw new BizException("密钥激活失败，请重试");
            }

            // 5. 合并更新结果到返回对象（确保调用方拿到最新值）
            keyInfo.setActivated((byte) 1);
            keyInfo.setActivationDevice(updateEntity.getActivationDevice());
            keyInfo.setActivationTime(updateEntity.getActivationTime());
            keyInfo.setRemark(updateEntity.getRemark());

            // 仅当 updateEntity 显式设置了字段时才覆盖（避免 null 覆盖原值）
            if (updateEntity.getRemainingActiveNum() != null) {
                keyInfo.setRemainingActiveNum(updateEntity.getRemainingActiveNum());
            }
            if (updateEntity.getTotalUses() != null) {
                keyInfo.setTotalUses(updateEntity.getTotalUses());
            }
            if (updateEntity.getRemainingUses() != null) {
                keyInfo.setRemainingUses(updateEntity.getRemainingUses());
            }
            if (updateEntity.getExpiryTime() != null) {
                keyInfo.setExpiryTime(updateEntity.getExpiryTime());
            }

            return keyInfo;

        } finally {
            redisLockUtil.unlock(lockKey);
        }
    }

    /**
     * 根据产品类型配置激活后的使用权益。
     * <p>
     * 注意：此方法只设置 {@code total_uses}, {@code remaining_uses}, {@code expiry_time}，
     * 其他字段（如 activated）由调用方设置。
     *
     * @param target 待更新的 KeyInfo 对象（非 null）
     * @param productType 产品类型标识（如 "ai_vip_hourly"）
     * @param activationTime 激活时间（用于计算过期时间）
     */
    private void configureUsageRights(KeyInfo target, String productType, LocalDateTime activationTime) {
        Objects.requireNonNull(target, "target cannot be null");
        Objects.requireNonNull(productType, "productType cannot be null");

        switch (productType) {
            case "ai_vip_hourly":
                target.setTotalUses(0);          // 0 = 无限制
                target.setRemainingUses(0);      // 无限制时必须为 0
                target.setExpiryTime(activationTime.plusHours(1));
                break;

            case "ai_basic_100":
                target.setTotalUses(100);
                target.setRemainingUses(100);    // 激活后可用 100 次
                target.setExpiryTime(null);      // 永            break;

            case "ai_pro":
                target.setTotalUses(0);
                target.setRemainingUses(0);
                target.setExpiryTime(activationTime.plusDays(30));
                break;

            default:
                // 安全兜底：1小时无限制
                log.warn("未知 productType: {}, 使用默认配置: 1小时无限制", productType);
                target.setTotalUses(0);
                target.setRemainingUses(0);
                target.setExpiryTime(activationTime.plusHours(1));
                break;
        }
    }

    private ValidationResult activeKeyValid(String apiKey, String deviceID) {
        // 1. 查询密钥
        KeyInfo keyInfo = keyInfoService.getByKeyValue(apiKey);
        if (keyInfo == null) {
            return ValidationResult.failure("密钥不存在", BizStatus.KEY_NOT_FOUND);
        }

        // 2. 校验激活资格（独立于 validateKey）
        // 2.1 状态必须启用
        if (keyInfo.getStatus() != 1) {
            return ValidationResult.failure("密钥已被禁用", BizStatus.KEY_INACTIVE);
        }

        // 2.2 如果激活过（activated = 1）
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
        Integer actNum = keyInfo.getActivationsNum();
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
            log.warn("激活次数配置异常: key={}, actNum={}, remAct={}", keyInfo.getKeyValue(),actNum, remAct);
            return ValidationResult.failure("激活次数配置异常", BizStatus.KEY_INVALID_STATE);
        }

        // 2.4 检查过期（未激活的密钥也可能过期）
        LocalDateTime expiryTime = keyInfo.getExpiryTime();
        if (expiryTime != null && expiryTime.isBefore(LocalDateTime.now())) {
            return ValidationResult.failure("密钥已过期", BizStatus.KEY_EXPIRED);
        }

        return ValidationResult.success(keyInfo);
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

        // 设置套餐类型
        keyInfo.setProductType(ProductType.AIGC_BASIC.getType());

        // 设置默认值，激活时再设置权益
        keyInfo.setTotalUses(0);
        keyInfo.setRemainingUses(0);
        keyInfo.setActivationsNum(0);
        keyInfo.setRemainingActiveNum(0);
        keyInfo.setActivated((byte) 0);

        keyInfo.setCreatedBy(1L);
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
