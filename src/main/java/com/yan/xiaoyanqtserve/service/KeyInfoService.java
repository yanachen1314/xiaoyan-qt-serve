package com.yan.xiaoyanqtserve.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yan.xiaoyanqtserve.entity.KeyInfo;
import com.baomidou.mybatisplus.extension.service.IService;
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
     * @param page 页码
     * @param pageSize 每页大小
     * @param status 密钥状态（可选）
     * @param activated 激活状态（可选）
     * @param productType 产品类型（可选）
     * @return 分页结果
     */
    Page<KeyInfo> getList(Integer page, Integer pageSize, Byte status, Byte activated, String productType);
}
