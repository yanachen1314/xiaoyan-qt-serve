package com.yan.xiaoyanqtserve;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.yan.xiaoyanqtserve.entity.SysConfig;
import com.yan.xiaoyanqtserve.service.SysConfigService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@Slf4j
@SpringBootTest
class XiaoyanQtServeApplicationTests {

    @Resource
    private SysConfigService sysConfigService;

    @Test
    void contextLoads() {
        for (int i = 0; i < 10; i++) {
            LambdaQueryWrapper<SysConfig> lambdaedQuery = Wrappers.lambdaQuery();
            lambdaedQuery.like(SysConfig::getConfigKey,"dashscope")
                    .last("ORDER BY RAND() LIMIT 1");
            List<SysConfig> sysConfigList = sysConfigService.list(lambdaedQuery);
            log.info(sysConfigList.get(0).getConfigId().toString());
        }

    }


}
