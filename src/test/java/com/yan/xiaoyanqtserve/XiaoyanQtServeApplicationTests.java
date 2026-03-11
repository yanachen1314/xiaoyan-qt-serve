package com.yan.xiaoyanqtserve;

import com.yan.xiaoyanqtserve.common.utils.LicenseKeyGenerator;
import com.yan.xiaoyanqtserve.entity.KeyInfo;
import com.yan.xiaoyanqtserve.service.KeyInfoService;
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
    private LicenseKeyGenerator licenseKeyGenerator;
    @Resource
    private KeyInfoService keyInfoService;
    @Resource
    private SysConfigService sysConfigService;

    @Test
    void contextLoads() {
        String value = sysConfigService.getValue("system.version");
        log.info("value: {}", value);


        List<KeyInfo> list = keyInfoService.list();
        log.info("list: {}", list.stream().limit(5));

        KeyInfo keyInfo = keyInfoService.getByKeyValue("KJTC3CF0-DN9P62EE-Z7HXEDS9-I70Q29JG");
        log.info("keyInfo: {}", keyInfo);
    }

}
