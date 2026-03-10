package com.yan.xiaoyanqtserve.service;

import com.yan.xiaoyanqtserve.common.dto.key.GenerateKeyDto;
import com.yan.xiaoyanqtserve.entity.KeyInfo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Optional;

/**
 * <p>
 * 密钥信息表 服务类
 * </p>
 *
 * @author 烟尘
 * @since 2026-02-03
 */
public interface KeyInfoService extends IService<KeyInfo> {

    /**
     * 激活密钥
     * @param apiKey API Key
     * @return 激活成功返回KeyInfo，否则返回Optional.empty()
     */
    Optional<KeyInfo> activateKey(String apiKey);

    /**
     * 扣减密钥使用次数
     * @param apiKey API Key
     * @return 扣减成功返回true，否则返回false
     */
    boolean decrementKeyUses(String apiKey);

    /**
     * 密钥生成
     * @param generateKeyDto 密钥生成参数
     */
    void generateKey(GenerateKeyDto generateKeyDto);
}

