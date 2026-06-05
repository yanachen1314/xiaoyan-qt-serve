package com.yan.xiaoyanqtserve.sys;

import com.yan.xiaoyanqtserve.common.Constants;
import com.yan.xiaoyanqtserve.common.utils.RedisUtil;
import com.yan.xiaoyanqtserve.entity.SysConfig;
import com.yan.xiaoyanqtserve.mapper.SysConfigMapper;
import com.yan.xiaoyanqtserve.service.impl.sys.SysConfigServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SysConfigServiceImplTest {

    private SysConfigMapper sysConfigMapper;
    private RedisUtil redisUtil;
    private SysConfigServiceImpl service;

    @BeforeEach
    void setUp() {
        sysConfigMapper = mock(SysConfigMapper.class);
        redisUtil = mock(RedisUtil.class);
        service = new SysConfigServiceImpl();
        ReflectionTestUtils.setField(service, "sysConfigMapper", sysConfigMapper);
        ReflectionTestUtils.setField(service, "redisUtil", redisUtil);
    }

    @Test
    @DisplayName("缓存未命中时从数据库读取配置并写回普通配置缓存")
    void getValueCachesDbConfigAfterCacheMiss() {
        SysConfig config = enabledConfig("system.foo", "bar");
        when(sysConfigMapper.selectList(any())).thenReturn(List.of(config));

        String value = service.getValue("system.foo");

        assertEquals("bar", value);
        verify(redisUtil).hSet(Constants.CONFIG_KEY, "system.foo", "bar");
    }

    @Test
    @DisplayName("模型列表缓存命中时返回模型配置缓存值")
    void getValueReturnsModelListCacheValue() {
        when(redisUtil.hGet(Constants.CONFIG_MODEL_LIST_KEY, "dashscope.model.test")).thenReturn("qwen-test");

        String value = service.getValue("dashscope.model.test");

        assertEquals("qwen-test", value);
        verify(sysConfigMapper, never()).selectList(any());
    }

    @Test
    @DisplayName("模型列表配置从数据库读取后写回模型列表缓存")
    void getValueCachesDbModelListConfigAfterCacheMiss() {
        SysConfig config = enabledConfig("dashscope.model.test", "qwen-test");
        when(sysConfigMapper.selectList(any())).thenReturn(List.of(config));

        String value = service.getValue("dashscope.model.test");

        assertEquals("qwen-test", value);
        verify(redisUtil).hSet(Constants.CONFIG_MODEL_LIST_KEY, "dashscope.model.test", "qwen-test");
    }

    private SysConfig enabledConfig(String key, String value) {
        SysConfig config = new SysConfig();
        config.setConfigKey(key);
        config.setConfigValue(value);
        config.setStatus(1);
        return config;
    }
}
