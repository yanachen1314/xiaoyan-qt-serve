package com.yan.xiaoyanqtserve.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yan.xiaoyanqtserve.common.dto.key.KeyInfoPageRequest;
import com.yan.xiaoyanqtserve.entity.KeyInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yan.xiaoyanqtserve.entity.ProductType;
import org.springframework.lang.Nullable;


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
     * 根据密钥值查询密钥信息
     * @param keyValue 密钥值
     * @return 密钥信息
     */
    @Nullable
    KeyInfo getByKeyValue(String keyValue);

    /**
     * 分页查询密钥列表
     * @param pageRequest 分页查询参数
     * @return 分页结果
     */
    Page<KeyInfo> getList(KeyInfoPageRequest pageRequest);

    /**
     * 更新密钥信息
     * @param keyInfo 密钥信息
     */
    void updateKeyInfo(KeyInfo keyInfo);

    /**
     * 获取随机免费卡密
     * @return 卡密信息，如果没有则返回 null
     */
    @Nullable
    KeyInfo getRandomFreeKey();

    /**
     * 获取随机售卖卡密（用于闲鱼售卖）
     * @return 卡密信息，如果没有则返回 null
     */
    @Nullable
    KeyInfo getRandomSaleKey(ProductType productType);
}
