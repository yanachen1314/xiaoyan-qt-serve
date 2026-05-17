package com.yan.xiaoyanqtserve;

import com.yan.xiaoyanqtserve.common.Constants;
import com.yan.xiaoyanqtserve.common.dto.key.GenerateKeyDto;
import com.yan.xiaoyanqtserve.common.enums.BizStatus;
import com.yan.xiaoyanqtserve.common.exception.BizException;
import com.yan.xiaoyanqtserve.common.utils.LicenseKeyGenerator;
import com.yan.xiaoyanqtserve.entity.KeyInfo;
import com.yan.xiaoyanqtserve.entity.ProductType;
import com.yan.xiaoyanqtserve.service.KeyInfoService;
import com.yan.xiaoyanqtserve.service.MailApplyRecordService;
import com.yan.xiaoyanqtserve.service.ProductTypeService;
import com.yan.xiaoyanqtserve.service.SysConfigService;
import com.yan.xiaoyanqtserve.service.impl.apikey.KeyInfoManagerService;
import com.yan.xiaoyanqtserve.service.impl.mail.MailApplyManagerService;
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

    @Resource
    private MailApplyRecordService mailApplyRecordService;
    @Resource
    private MailApplyManagerService mailApplyManagerService;


    @Test
    void contextLoads() {
        String token = "9q4DA7V65MkLxHUswh_LG3uj6aLG6FRTP0DQDtSEDgHXAH6T7xz5DyciZuW6ph2e2tFa0EEBsCobuEtDd8EKn1XDDDnBL76ise6itbiPTZACegfWYluBgGVwNHFqjbRkxKCwS4S4TKZkXKMMKkY6TBhDM-PLAawrJJZgjKDWIjs";
        // 增加点击次数
        mailApplyRecordService.increaseClickCountByToken(token);
        // 判断是否为第一次点击
        Boolean firstClick = mailApplyManagerService.isFirstClickAfterIncClickCount(token);
        log.info("=================> First click: {}", firstClick);
    }


}
