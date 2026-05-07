package com.yan.xiaoyanqtserve;

import com.yan.xiaoyanqtserve.common.Constants;
import com.yan.xiaoyanqtserve.common.dto.key.GenerateKeyDto;
import com.yan.xiaoyanqtserve.common.enums.BizStatus;
import com.yan.xiaoyanqtserve.common.exception.BizException;
import com.yan.xiaoyanqtserve.common.utils.LicenseKeyGenerator;
import com.yan.xiaoyanqtserve.entity.KeyInfo;
import com.yan.xiaoyanqtserve.entity.ProductType;
import com.yan.xiaoyanqtserve.service.KeyInfoService;
import com.yan.xiaoyanqtserve.service.ProductTypeService;
import com.yan.xiaoyanqtserve.service.SysConfigService;
import com.yan.xiaoyanqtserve.service.impl.apikey.KeyInfoManagerService;
import com.yan.xiaoyanqtserve.task.MailRecordSyncTask;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@Slf4j
@SpringBootTest
class XiaoyanQtServeApplicationTests {

    @Resource
    private  SysConfigService sysConfigService;
    @Resource
    private  ProductTypeService productTypeService;
    @Resource
    private  KeyInfoService keyInfoService;
    @Resource
    private  KeyInfoManagerService keyInfoManagerService;
    @Resource
    private  LicenseKeyGenerator licenseKeyGenerator;
    @Resource
    private MailRecordSyncTask mailRecordSyncTask;

    @Test
    void contextLoads() {
      mailRecordSyncTask.resetDailyFreeKeyCountAndReplenishPool();
    }


}
