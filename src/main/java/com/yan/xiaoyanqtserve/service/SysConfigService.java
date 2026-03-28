package com.yan.xiaoyanqtserve.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yan.xiaoyanqtserve.common.dto.adminUser.ConfigPageRequest;
import com.yan.xiaoyanqtserve.common.dto.adminUser.SysConfigDto;
import com.yan.xiaoyanqtserve.common.enums.ConfigType;
import com.yan.xiaoyanqtserve.entity.SysConfig;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 系统全局可变配置表 服务类
 * </p>
 *
 * @author 烟尘
 * @since 2026-03-10
 */
public interface SysConfigService extends IService<SysConfig> {
    /**
     * 重新加载缓存（所有配置），可在修改后调用
     */
    void reloadCache();

    /**
     * 分页查询配置
     *
     * @param configPageRequest 分页查询参数
     * @return IPage<SysConfig> 分页结果
     */
    IPage<SysConfig> listByPage(ConfigPageRequest configPageRequest);


    /**
     * 根据 key 获取 String 类型配置值
     *
     * @param key 配置键
     * @return 配置值，找不到返回 null
     */
    String getValue(String key);

    /**
     * 根据 key 获取指定类型的配置值
     *
     * @param key   配置键
     * @param type  配置类型，{@code ConfigType != JSON}
     * @param <T>   目标泛型
     * @return 转换后的值，找不到或转换失败可抛出异常
     */
    <T> T getValueNonJson(String key, ConfigType type);
    /**
     * 根据 key 获取指定类型的配置值
     *
     * @param key   配置键
     * @param type  配置类型
     * @param clazz 目标类型，type = JSON时为要转换的对象
     * @param <T>   目标泛型
     * @return 转换后的值，找不到或转换失败可抛出异常
     */
    <T> T getValue(String key, ConfigType type, Class<T> clazz);

    /**
     * 根据 key 获取配置项列表
     *
     * @param key       配置项 key
     * @param isLike    是否模糊匹配
     * @return 配置项列表
     */
    List<SysConfig> getConfigByKey(String key, boolean isLike);

    /**
     * 新增配置
     *
     * @param config 配置实体
     */
    void saveConfig(SysConfigDto config);

    /**
     * 更新配置
     *
     * @param config 配置实体
     */
    void update(SysConfig config);

    /**
     * 删除配置
     *
     * @param configId 配置 ID
     */
    void delete(Long configId);

    /**
     * 获取可用模型列表
     *
     * @return 模型列表
     */
    List<String> getModelList();

    /**
     * 获取所有配置分组列表
     *
     * @return 分组列表
     */
    List<String> getGroupList();
}
