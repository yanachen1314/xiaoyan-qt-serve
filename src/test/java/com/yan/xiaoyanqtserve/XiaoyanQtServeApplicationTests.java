package com.yan.xiaoyanqtserve;

import com.yan.xiaoyanqtserve.common.utils.LicenseKeyGenerator;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class XiaoyanQtServeApplicationTests {
@Resource
private LicenseKeyGenerator licenseKeyGenerator;

    @Test
    void contextLoads() {
        String s = licenseKeyGenerator.generateKey();
        System.out.println(s);
        System.out.println(licenseKeyGenerator.verify(s));
        System.out.println(licenseKeyGenerator.verify("2PQFJ1JR-XVP1PMXU-RDC04LOC-DAFOYPXI"));
        System.out.println(licenseKeyGenerator.verify("2PQFJ1JR-XVP1PMXU-RDC04LOC-DAFOYPX1"));
    }

}
