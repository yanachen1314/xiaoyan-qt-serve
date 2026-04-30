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

    @Test
    void contextLoads() {
        // 开始真正创建ApiKey的数据库记录
        // 生成若干密钥
        List<String> keyList = licenseKeyGenerator.generateKey(1);
        String freeKeyValue = keyList.get(0);
        try {
            // 构建密钥生成参数
            GenerateKeyDto generateKeyDto = new GenerateKeyDto();
            generateKeyDto.setKeyNumber(200);
            generateKeyDto.setUsageType(Constants.FREE_KEY_USAGE_TYPE);
            generateKeyDto.setRemark("系统自动生成-免费密钥");

            ProductType productType = productTypeService.getByProductCode(Constants.FREE_KEY_PRODUCT_CODE);
            if(productType == null){
                log.warn("产品类型不存在，无法生成密钥：{}", Constants.FREE_KEY_PRODUCT_CODE);
                throw new BizException("产品类型不存在！");
            }
            generateKeyDto.setProductTypeId(productType.getId().intValue());
            KeyInfo keyInfo = keyInfoManagerService.buildKeyInfo(freeKeyValue, 1L, generateKeyDto, productType);
            keyInfo.setSelled((byte) 1);
            keyInfoService.save(keyInfo);
        } catch (Exception e) {
            log.error("【分发免费密钥】保存密钥信息失败，密钥值: {}", freeKeyValue);
            throw new BizException(BizStatus.ERROR, "保存卡密信息失败！",e);
        }

    }


}
