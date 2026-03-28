package com.yan.xiaoyanqtserve.service.impl;

import com.yan.xiaoyanqtserve.common.Constants;
import com.yan.xiaoyanqtserve.common.dto.MaintenancePeriod;
import com.yan.xiaoyanqtserve.common.utils.RedisUtil;
import com.yan.xiaoyanqtserve.service.SysConfigService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * 维护服务
 * @author yanchen
 * @since 2026/3/28 11:34
 */
@Slf4j
@Service
public class MaintenanceService {

    @Resource
    private RedisUtil redisUtil;

    /**
     * 系统是否正在维护
     * @return 是否正在维护
     */
    public boolean isSystemInMaintenance() {
        MaintenancePeriod maintenancePeriod = getMaintenancePeriod();
        if (maintenancePeriod == null) {
            return false;
        }
        return maintenancePeriod.isInMaintenance(LocalDateTime.now());
    }

    /**
     * 获取维护时间
     * @return 维护时间
     */
    public MaintenancePeriod getMaintenancePeriod() {
        String value = (String) redisUtil.get(Constants.CONFIG_KEY + "system.maintenance");
        if (StringUtils.hasText(value)) {
            MaintenancePeriod maintenancePeriod = MaintenancePeriod.parse(value);
            if (maintenancePeriod != null) {
                return maintenancePeriod;
            }
            log.warn("解析维护时间失败，请检查配置");
            return null;
        }
        log.warn("未查询到维护时间，请检查配置");
        return null;
    }
}
