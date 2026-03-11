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
import java.util.List;

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
    public ValidationResult validateKey(String apiKey) {
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

        // 5. 校验激活次数是否充足（仅在已激活前提下进行）
        Integer actNum = keyInfo.getActivationsNum();
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
        }

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
     * 激活密钥
     *
     * @param keyActiveDto 激活密钥参数
     */
    @Transactional(rollbackFor = Exception.class)
    public KeyInfo activateKey(KeyActiveDto keyActiveDto) {
        // 锁的唯一标识
        String lockKey = "Lock_" + keyActiveDto.getApiKey();

        if (!redisLockUtil.lock(lockKey, 10)) {
            throw new BizException("系统繁忙，请稍后再试！");
        }

        try {
            // 根据 API Key 查询密钥
            ValidationResult validationResult = validateKey(keyActiveDto.getApiKey());

            // 校验失败，返回错误信息
            if (!validationResult.isSuccess()) {
                throw new BizException(validationResult.getErrorCode(),
                        validationResult.getErrorMessage() == null ?
                                validationResult.getErrorCode().msg() :
                                validationResult.getErrorMessage());
            }

            KeyInfo keyInfo = validationResult.getKeyInfo();
            keyInfo.setTotalUses(0);
            keyInfo.setRemainingUses(0);
            keyInfo.setActivationsNum(0);
            keyInfo.setRemainingActiveNum(keyInfo.getRemainingActiveNum() - 1);
            keyInfo.setActivated((byte) 1);

            // 更新密钥的激活设备
            keyInfo.setActivationDevice(keyActiveDto.getDeviceID());
            // 更新密钥的激活时间
            LocalDateTime now = LocalDateTime.now();
            keyInfo.setActivationTime(now);
            // 设置密钥的到期时间（一个小时后过期）
            keyInfo.setExpiryTime(now.plusHours(1));
            keyInfo.setRemark("一小时无限制使用的密钥");
            // 保存更新后的密钥信息
            boolean updated = keyInfoService.updateById(keyInfo);
            if (!updated) {
                throw new BizException("密钥激活异常，请稍后再试！");
            }
            return keyInfo;
        } finally {
            // 一定要在finally中释放锁，确保异常时也能释放
            redisLockUtil.unlock(lockKey);
        }

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
