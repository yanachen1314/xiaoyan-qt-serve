package com.yan.xiaoyanqtserve.service.impl.sys;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yan.xiaoyanqtserve.common.Constants;
import com.yan.xiaoyanqtserve.common.dto.adminUser.ConfigPageRequest;
import com.yan.xiaoyanqtserve.common.dto.adminUser.SysConfigDto;
import com.yan.xiaoyanqtserve.common.enums.ConfigType;
import com.yan.xiaoyanqtserve.common.utils.RedisUtil;
import com.yan.xiaoyanqtserve.entity.SysConfig;
import com.yan.xiaoyanqtserve.common.exception.BizException;
import com.yan.xiaoyanqtserve.mapper.SysConfigMapper;
import com.yan.xiaoyanqtserve.service.SysConfigService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 系统全局可变配置表 服务实现类
 *
 * @author 烟尘
 * @since 2025-07-27
 */
@Slf4j
@Service
public class SysConfigServiceImpl extends ServiceImpl<SysConfigMapper, SysConfig> implements SysConfigService {
    @Resource
    private SysConfigMapper sysConfigMapper;

    @Resource
    private RedisUtil redisUtil;

    /**
     * JSON 解析，用于复杂类型转换
     */
    @Resource
    private ObjectMapper objectMapper;

    /**
     * 应用启动时将 DB 中配置全部加载到 Redis
     */
    @PostConstruct
    public void init() {
        reloadCache();
    }

    /**
     * 根据 key 获取指定类型的配置值
     *
     * @param key   配置键
     * @param type  配置类型，{@code ConfigType != JSON}
     * @param <T>   目标泛型
     * @return 转换后的值，找不到或转换失败可抛出异常
     */
    @Override
    public <T> T getValueNonJson(String key, ConfigType type) {
        if (ConfigType.JSON != type) {
            return getValue(key, type, null);
        } else {
            throw new BizException("不支持的配置类型: " + type);
        }
    }

    /**
     * 根据 key 获取指定类型的配置值
     *
     * @param key   配置键
     * @param type  配置类型
     * @param clazz 目标类型，type = JSON时为要转换的对象
     * @param <T>   目标泛型
     * @return 转换后的值，找不到或转换失败可抛出异常
     */
    @Override
    public <T> T getValue(String key, ConfigType type, Class<T> clazz) {
        String val = getValue(key);
        try {
            return switch (type) {
                case STRING -> clazz.cast(val);
                case INT -> clazz.cast(Integer.valueOf(val));
                case BOOLEAN -> clazz.cast(Boolean.valueOf(val));
                case JSON -> objectMapper.readValue(val, clazz);
            };
        } catch (Exception e) {
            throw new BizException("配置类型转换失败: key=" + key + ", target=" + clazz.getSimpleName());
        }
    }

    /**
     * 获取配置项的原始值
     *
     * @param key 配置键（精确匹配）
     * @return 配置值（默认类型为String）
     */
    @Override
    public String getValue(String key) {
        // 获取缓存值, 找不到则从数据库中获取
        Object val = redisUtil.hGet(Constants.CONFIG_KEY, key);
        Object model_val = redisUtil.hGet(Constants.CONFIG_MODEL_LIST_KEY, key);
        if (Objects.nonNull(val) || Objects.nonNull(model_val)) {
            return val.toString();
        }

        List<SysConfig> sysConfigs = getConfigByKey(key, false);
        SysConfig config = sysConfigs.size() == 1 ? sysConfigs.get(0) : null;
        // 找不到则抛出异常
        if (Objects.isNull(config) || !StringUtils.hasText(config.getConfigValue())) {
            throw new BizException("配置不存在: " + key);
        }
        return config.getConfigValue();
    }

    /**
     * 根据 key 获取配置项列表
     * <p>直接从数据库查询</p>
     * @param key    配置项 key
     * @param enableLike 是否开启模糊匹配，false 为精确匹配
     * @param enableStatus 是否开启状态匹配，false 不添加status查询参数
     * @return 配置项列表
     */
    public List<SysConfig> getConfigByKey(String key, boolean enableLike, boolean enableStatus, Integer status) {
        LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(enableStatus, SysConfig::getStatus, status);
        // 配置项 key, 是否进行模糊匹配
        if (enableLike) {
            wrapper.like(SysConfig::getConfigKey, key);
        } else {
            wrapper.eq(SysConfig::getConfigKey, key);
        }
        return sysConfigMapper.selectList(wrapper);
    }
    public List<SysConfig> getConfigByKey(String key, boolean enableLike) {
        // 默认开启状态匹配，status == 1
        return getConfigByKey(key, enableLike, true, 1);
    }

    // 判断是否为模型列表配置项，即以 "dashscope.model" 开头的配置项
    private boolean isModelList(String key) {
        return key.startsWith(Constants.CONFIG_MODEL_LIST_PREFIX);
    }

    // 当配置更新或新增时，判断如何处理 Redis 缓存，确保数据一致性
    private void onChangeDealRedisCache(SysConfig config) {
        String configKey = config.getConfigKey();
        if (config.getStatus() != null && config.getStatus() == 1) {
            // 如果状态为1，则同步更新 Redis
            if (isModelList(configKey)) {
                // 模型列表的配置项
                redisUtil.hSet(Constants.CONFIG_MODEL_LIST_KEY, configKey, config.getConfigValue());
            } else {
                // 其他配置项
                redisUtil.hSet(Constants.CONFIG_KEY, configKey, config.getConfigValue());
                // 如果是系统维护配置项，则同步更新系统维护状态
                if(configKey.equals(Constants.SYSTEM_MAINTENANCE_KEY)){
                    redisUtil.hSet(Constants.CONFIG_KEY, Constants.SYSTEM_MAINTENANCE_STATUS, config.getStatus() == 1);
                }
            }
        } else {
            // 如果状态为0，则删除缓存
            if (isModelList(configKey)) {
                // 模型列表的配置项
                redisUtil.hDel(Constants.CONFIG_MODEL_LIST_KEY, configKey);
            } else {
                // 其他配置项
                redisUtil.hDel(Constants.CONFIG_KEY, configKey);
                // 如果是系统维护配置项，则同步删除系统维护状态
                if(configKey.equals(Constants.SYSTEM_MAINTENANCE_KEY)){
                    redisUtil.hDel(Constants.CONFIG_KEY, Constants.SYSTEM_MAINTENANCE_STATUS);
                }
            }
        }
    }

    /**
     * 获取可用模型列表
     *
     * @return 模型列表，找不到则返回空列表
     */
    @Override
    public List<String> getModelList() {
        List<Object> modelValueList = redisUtil.hGetAllValues(Constants.CONFIG_MODEL_LIST_KEY);

        if (Objects.nonNull(modelValueList) && !modelValueList.isEmpty()) {
            return modelValueList.stream()
                    .filter(Objects::nonNull)
                    .map(Object::toString)
                    .collect(Collectors.toList());
        }
        // 缓存数据为空，从数据库查询模型列表配置
        List<SysConfig> sysConfigs = getConfigByKey(Constants.CONFIG_MODEL_LIST_PREFIX, true);

        if (sysConfigs.isEmpty()) {
            return Collections.emptyList();
        }

        // 重新保存到缓存
        for (SysConfig config : sysConfigs) {
            redisUtil.hSet(Constants.CONFIG_MODEL_LIST_KEY, config.getConfigKey(), config.getConfigValue());
        }

        // 返回模型列表值
        return sysConfigs.stream()
                .map(SysConfig::getConfigValue)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

    }

    @Override
    public void reloadCache() {
        log.info("重新加载系统配置缓存...");
        // 清空旧缓存
        redisUtil.delete(Constants.CONFIG_KEY);
        redisUtil.delete(Constants.CONFIG_MODEL_LIST_KEY);
        // 从 DB 重新加载所有配置
        LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysConfig::getStatus, 1);
        List<SysConfig> all = sysConfigMapper.selectList(wrapper);
        for (SysConfig cfg : all) {
            onChangeDealRedisCache(cfg);
        }
    }

    @Override
    public void saveConfig(SysConfigDto config) {
        SysConfig sysConfig = new SysConfig();
        BeanUtils.copyProperties(config, sysConfig);
        sysConfigMapper.insert(sysConfig);

        onChangeDealRedisCache(sysConfig);
    }

    @Override
    public void update(SysConfig config) {
        if (config.getConfigId() == null) {
            throw new BizException("参数错误");
        }
        sysConfigMapper.updateById(config);
        onChangeDealRedisCache(config);
    }

    @Override
    public void delete(Long configId) {
        if (configId == null) {
            throw new BizException("参数错误");
        }
        SysConfig cfg = sysConfigMapper.selectById(configId);
        if (cfg != null) {
            sysConfigMapper.deleteById(configId);
            // 同步删除 Redis
            if (isModelList(cfg.getConfigKey())) {
                redisUtil.hDel(Constants.CONFIG_MODEL_LIST_KEY, cfg.getConfigKey());
            } else {
                redisUtil.hDel(Constants.CONFIG_KEY, cfg.getConfigKey());
            }
        }
    }


    /**
     * 分页查询配置
     * <p>直接从数据库查询</p>
     * @param configPageRequest 分页查询参数
     * @return IPage<SysConfig> 分页结果
     */
    @Override
    public IPage<SysConfig> listByPage(ConfigPageRequest configPageRequest) {
        // 构建分页对象
        Page<SysConfig> page = new Page<>(configPageRequest.getCurrentPage(), configPageRequest.getPageSize());

        // 构建查询条件
        LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<>();

        // 配置项 key 模糊匹配
        wrapper.like(StringUtils.hasText(configPageRequest.getConfigKey()),
                        SysConfig::getConfigKey, configPageRequest.getConfigKey())
                // 配置项分组匹配
                .eq(StringUtils.hasText(configPageRequest.getConfigGroup()),
                        SysConfig::getConfigGroup, configPageRequest.getConfigGroup())
                // 配置项类型匹配
                .eq(StringUtils.hasText(configPageRequest.getConfigType()),
                        SysConfig::getConfigType, configPageRequest.getConfigType())
                // 排序：默认按创建时间升序（1,2,3）
                .orderByAsc(SysConfig::getCreatedAt);

        // 执行分页查询
        return sysConfigMapper.selectPage(page, wrapper);
    }

    /**
     * 获取所有配置分组列表
     * <p>直接从数据库查询</p>
     * @return 分组列表
     */
    @Override
    public List<String> getGroupList() {
        LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(SysConfig::getConfigGroup)
                .groupBy(SysConfig::getConfigGroup);
        List<SysConfig> configList = sysConfigMapper.selectList(wrapper);
        return configList.stream()
                .map(SysConfig::getConfigGroup)
                .filter(StringUtils::hasText)
                .collect(Collectors.toList());
    }


}
